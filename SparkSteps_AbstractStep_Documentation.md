# Spark Step Design — AbstractStep & AbstractStepMap

## 🇫🇷 Fonctionnement des étapes Spark

### `AbstractStep<T>`
Cette classe permet de :
- Charger des données depuis des sources (CSV, Parquet, Hive…)
- Appliquer une logique dans `launch`
- Exporter les données

**Exemples** :
- `ArivaLoad` : charge un CSV vers des objets `Ariva`
- `ArivaSave` : exporte en Parquet

### `AbstractStepMap<T>`
Permet de transformer ligne par ligne avec la méthode `execute(T row)`

**Exemple** :
- `ArivaTransform` : ajoute une date d’ingestion

### Orchestration
Chaque étape est annotée avec `@Step(StepConstants.XYZ)` pour garantir l’ordre d’exécution.

---

## 🇬🇧 Spark Step Structure

### `AbstractStep<T>`
This class allows:
- Loading data from sources (CSV, Parquet, Hive…)
- Custom logic in `launch`
- Exporting transformed datasets

**Examples**:
- `ArivaLoad`: load CSV into `Ariva` objects
- `ArivaSave`: export to Parquet

### `AbstractStepMap<T>`
Used to transform each record via `execute(T row)`

**Example**:
- `ArivaTransform`: adds ingestion date

### Execution Order
Each step uses `@Step(StepConstants.XYZ)` to define execution sequence.

---

## 📊 Pipeline Diagram

![Pipeline Diagram](SparkSteps_Pipeline_Diagram.png)
