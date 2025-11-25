# Guide d'Installation et de Démarrage

## 🔴 Problèmes identifiés

1. **Java n'est pas installé ou pas dans le PATH**
2. **Maven n'est pas installé correctement** (il y a un conflit avec un module Python)
3. **La base de données PostgreSQL doit être créée**

## ✅ Solutions étape par étape

### 1. Installer Java 17

**Option A : Télécharger depuis le site officiel**
1. Aller sur https://adoptium.net/ (ou https://www.oracle.com/java/technologies/downloads/#java17)
2. Télécharger **JDK 17** pour Windows (x64)
3. Installer le JDK
4. Ajouter Java au PATH :
   - Ouvrir "Variables d'environnement" dans Windows
   - Ajouter le chemin vers `bin` du JDK (ex: `C:\Program Files\Java\jdk-17\bin`)
   - Ou utiliser : `C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot\bin`

**Option B : Utiliser Chocolatey (si installé)**
```powershell
choco install openjdk17
```

**Vérifier l'installation :**
```powershell
java -version
```
Vous devriez voir quelque chose comme : `openjdk version "17.x.x"`

### 2. Installer Maven

**Option A : Télécharger depuis le site officiel**
1. Aller sur https://maven.apache.org/download.cgi
2. Télécharger `apache-maven-3.9.x-bin.zip`
3. Extraire dans `C:\Program Files\Apache\maven`
4. Ajouter `C:\Program Files\Apache\maven\bin` au PATH

**Option B : Utiliser Chocolatey**
```powershell
choco install maven
```

**Vérifier l'installation :**
```powershell
mvn -version
```

**⚠️ IMPORTANT : Si vous avez encore l'erreur avec Python**
1. Vérifier quel `mvn` est utilisé : `where.exe mvn`
2. Si c'est Python, désinstaller le package Python : `pip uninstall mvn`
3. Vérifier que le vrai Maven est dans le PATH

### 3. Créer la base de données PostgreSQL

**Méthode 1 : Via pgAdmin**
1. Ouvrir pgAdmin
2. Se connecter au serveur PostgreSQL
3. Clic droit sur "Databases" → "Create" → "Database"
4. Nom : `feedback_db`
5. Cliquer "Save"

**Méthode 2 : Via PowerShell avec psql**
```powershell
# Se connecter à PostgreSQL (remplacer postgres par votre utilisateur si différent)
psql -U postgres

# Dans psql, exécuter :
CREATE DATABASE feedback_db;

# Sortir
\q
```

**Méthode 3 : Via ligne de commande directe**
```powershell
psql -U postgres -c "CREATE DATABASE feedback_db;"
```

### 4. Configurer les variables d'environnement (si nécessaire)

Si Java ou Maven ne sont toujours pas reconnus après installation, ajouter manuellement au PATH :

1. Ouvrir "Variables d'environnement" :
   - Windows + R → `sysdm.cpl` → Onglet "Avancé" → "Variables d'environnement"
   - Ou rechercher "Variables d'environnement" dans Windows

2. Modifier la variable "Path" :
   - Ajouter le chemin vers Java : `C:\Program Files\Java\jdk-17\bin`
   - Ajouter le chemin vers Maven : `C:\Program Files\Apache\maven\bin`

3. **Redémarrer PowerShell/terminal** après modification

### 5. Vérifier la configuration dans application.yml

Assurez-vous que dans `src/main/resources/application.yml` :
- Le port PostgreSQL est correct (par défaut 5432)
- Le username est `postgres` (ou votre utilisateur)
- Le password est correct (vous l'avez déjà configuré : `A190077abb`)
- La base de données est `feedback_db`

### 6. Démarrer le projet

**Option A : Avec Maven (si installé)**
```powershell
mvn clean install
mvn spring-boot:run
```

**Option B : Avec IntelliJ IDEA ou Eclipse**
- Ouvrir le projet dans l'IDE
- L'IDE devrait détecter automatiquement Maven et télécharger les dépendances
- Cliquer sur "Run" sur la classe `FeedbackServiceApplication`

**Option C : Créer un wrapper Maven (recommandé)**

Si vous continuez à avoir des problèmes avec Maven, je peux vous aider à créer le wrapper Maven qui télécharge Maven automatiquement.

## 🧪 Tests de vérification

Après avoir installé tout :

```powershell
# Vérifier Java
java -version

# Vérifier Maven (devrait montrer Apache Maven, pas Python)
mvn -version

# Vérifier PostgreSQL (si psql est installé)
psql --version
```

## ❓ Si ça ne fonctionne toujours pas

Envoyez-moi :
1. Le résultat de `java -version`
2. Le résultat de `mvn -version`
3. Le message d'erreur complet quand vous essayez de démarrer

Je vous aiderai à résoudre le problème spécifique !

