# Script PowerShell pour créer la base de données PostgreSQL
# Exécuter : .\create_database.ps1

Write-Host "Creation de la base de données feedback_db..." -ForegroundColor Green

# Paramètres de connexion (modifier si nécessaire)
$dbName = "feedback_db"
$dbUser = "postgres"
$dbHost = "localhost"
$dbPort = "5432"

# Demander le mot de passe
$securePassword = Read-Host "Entrez le mot de passe PostgreSQL pour l'utilisateur '$dbUser'" -AsSecureString
$dbPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword))

# Créer la variable d'environnement temporaire pour psql
$env:PGPASSWORD = $dbPassword

try {
    # Vérifier si psql est disponible
    $psqlPath = Get-Command psql -ErrorAction SilentlyContinue
    if (-not $psqlPath) {
        Write-Host "ERREUR: psql n'est pas trouvé dans le PATH." -ForegroundColor Red
        Write-Host "Assurez-vous que PostgreSQL est installé et que psql est dans le PATH." -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Alternative: Créez la base manuellement:" -ForegroundColor Yellow
        Write-Host "1. Ouvrez pgAdmin" -ForegroundColor Cyan
        Write-Host "2. Connectez-vous au serveur PostgreSQL" -ForegroundColor Cyan
        Write-Host "3. Clic droit sur 'Databases' -> 'Create' -> 'Database'" -ForegroundColor Cyan
        Write-Host "4. Nom: feedback_db" -ForegroundColor Cyan
        Write-Host "5. Cliquez 'Save'" -ForegroundColor Cyan
        exit 1
    }

    # Vérifier si la base existe déjà
    $existingDb = psql -U $dbUser -h $dbHost -p $dbPort -tAc "SELECT 1 FROM pg_database WHERE datname='$dbName'" 2>$null
    
    if ($existingDb -eq "1") {
        Write-Host "La base de données '$dbName' existe déjà." -ForegroundColor Yellow
        $response = Read-Host "Voulez-vous la supprimer et la recréer? (o/N)"
        if ($response -eq "o" -or $response -eq "O") {
            Write-Host "Suppression de la base existante..." -ForegroundColor Yellow
            psql -U $dbUser -h $dbHost -p $dbPort -c "DROP DATABASE IF EXISTS $dbName;" 2>&1 | Out-Null
        } else {
            Write-Host "Abandon de l'operation." -ForegroundColor Yellow
            exit 0
        }
    }

    # Créer la base de données
    Write-Host "Creation de la base de données '$dbName'..." -ForegroundColor Cyan
    $result = psql -U $dbUser -h $dbHost -p $dbPort -c "CREATE DATABASE $dbName;" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Base de donnees '$dbName' creee avec succes!" -ForegroundColor Green
    } else {
        Write-Host "ERREUR lors de la creation:" -ForegroundColor Red
        Write-Host $result -ForegroundColor Red
        exit 1
    }
    
} catch {
    Write-Host "ERREUR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    # Nettoyer la variable d'environnement
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "Base de donnees prete! Vous pouvez maintenant demarrer le microservice." -ForegroundColor Green

