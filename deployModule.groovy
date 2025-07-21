def call(Map config) {
  def moduleName = config.moduleName
  def overrideVersion = config.overrideVersion ?: ""

  stage("Build & Deploy ${moduleName}") {
    dir(moduleName) {

      def version = ""

      stage("🔧 Maven Build") {
        sh "mvn clean install -pl :${moduleName} -am -DskipTests"
      }

      stage("📦 Get Version") {
        def pom = readMavenPom file: "pom.xml"
        version = overrideVersion ?: pom.version
      }

      stage("📁 Prepare Bundle") {
        sh """
          mkdir -p target/lib
          cp ../module1/target/*.jar target/lib/module1.jar || true
          cp ../module2/target/*.jar target/lib/module2.jar || true
          cp target/*.jar target/lib/${moduleName}.jar
        """
        writeFile file: "target/manifestations.json", text: """{
  "module1": "${version}",
  "module2": "${version}",
  "${moduleName}": "${version}",
  "buildDate": "${new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone('UTC'))}"
}"""
      }

      stage("🧩 Create ZIP") {
        dir("target") {
          sh "zip -r release-bundle-clean.zip lib manifestations.json"
        }
      }

      stage("🚀 Upload JARs to JFrog") {
        withCredentials([usernamePassword(credentialsId: 'JFROG_CRED_ID', usernameVariable: 'JFROG_USER', passwordVariable: 'JFROG_PASS')]) {
          sh """
            curl -u$JFROG_USER:$JFROG_PASS -T target/lib/module1.jar https://ton-jfrog/artifactory/libs-release-local/module1/${version}/module1-${version}.jar || true
            curl -u$JFROG_USER:$JFROG_PASS -T target/lib/module2.jar https://ton-jfrog/artifactory/libs-release-local/module2/${version}/module2-${version}.jar || true
            curl -u$JFROG_USER:$JFROG_PASS -T target/lib/${moduleName}.jar https://ton-jfrog/artifactory/libs-release-local/${moduleName}/${version}/${moduleName}-${version}.jar
          """
        }
      }

      stage("☁️ Upload ZIP to Azure") {
        echo "Uploading ZIP for ${moduleName} to Azure..."
      }
    }
  }
}
