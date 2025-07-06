# Create PostgreSQL User
# اسکریپت ساخت کاربر PostgreSQL

Write-Host "Creating PostgreSQL user..." -ForegroundColor Blue

$PSQL_PATH = "C:\Program Files\PostgreSQL\17\bin\psql.exe"
$DB_USER = "food-ordering-project"
$DB_PASSWORD = "food-ordering-project"
$DB_NAME = "food_ordering_prod"

# اسکریپت ساخت کاربر
$createUserScript = @"
-- حذف کاربر اگر وجود دارد
DROP USER IF EXISTS $DB_USER;

-- ایجاد کاربر جدید
CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';

-- ایجاد دیتابیس اگر وجود ندارد
SELECT 'CREATE DATABASE $DB_NAME OWNER $DB_USER'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB_NAME')\gexec

-- اعطای مجوزها
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
GRANT CONNECT ON DATABASE $DB_NAME TO $DB_USER;

-- اتصال به دیتابیس و اعطای مجوزهای schema
\c $DB_NAME;
GRANT ALL ON SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $DB_USER;

SELECT 'User $DB_USER created successfully!' as result;
"@

# ذخیره اسکریپت در فایل موقت
$tempScriptPath = "$env:TEMP\create_user.sql"
$createUserScript | Out-File -FilePath $tempScriptPath -Encoding UTF8

try {
    Write-Host "Executing user creation script..." -ForegroundColor Blue
    & $PSQL_PATH -U postgres -f $tempScriptPath
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "User created successfully!" -ForegroundColor Green
    } else {
        Write-Host "Error creating user" -ForegroundColor Red
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    Remove-Item $tempScriptPath -ErrorAction SilentlyContinue
}

Write-Host "User creation completed." -ForegroundColor Blue 