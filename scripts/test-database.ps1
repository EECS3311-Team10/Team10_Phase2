param([string]$Image = 'postgres:16-alpine')
$ErrorActionPreference = 'Stop'
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$taskContainer = 'consultlink-schema-test-' + [Guid]::NewGuid().ToString('N').Substring(0,12)
$taskCreated = $false
function Assert-NativeSuccess([string]$Message) {
    if ($LASTEXITCODE -ne 0) { throw $Message }
}
try {
    & docker version --format '{{.Server.Version}}'
    Assert-NativeSuccess 'Docker engine is unavailable. Start Docker Desktop before running this test.'
    & docker image inspect $Image --format '{{.Id}}'
    Assert-NativeSuccess "Image $Image is not cached. Obtain the official PostgreSQL 16 image before running this script."
    # No host port, no external network, no persistent volume, and no real credentials.
    & docker run --detach --rm --pull never --network none --name $taskContainer `
        -e POSTGRES_HOST_AUTH_METHOD=trust -e POSTGRES_DB=consultlink_test $Image
    Assert-NativeSuccess 'Could not create isolated database test container.'
    $taskCreated = $true
    $taskReady = $false
    for ($taskAttempt = 0; $taskAttempt -lt 30; $taskAttempt++) {
        & docker exec $taskContainer pg_isready -U postgres -d consultlink_test 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) { $taskReady = $true; break }
        Start-Sleep -Milliseconds 500
    }
    if (-not $taskReady) { throw 'Isolated PostgreSQL did not become ready.' }
    & docker cp (Join-Path $taskRoot 'database\migrations\V1__consultlink_core.sql') "${taskContainer}:/tmp/schema.sql"
    Assert-NativeSuccess 'Could not copy migration.'
    & docker cp (Join-Path $taskRoot 'database\tests\constraints.sql') "${taskContainer}:/tmp/tests.sql"
    Assert-NativeSuccess 'Could not copy SQL tests.'
    & docker exec $taskContainer psql -X -U postgres -d consultlink_test -v ON_ERROR_STOP=1 -f /tmp/schema.sql
    Assert-NativeSuccess 'Schema migration failed.'
    & docker exec $taskContainer psql -X -U postgres -d consultlink_test -v ON_ERROR_STOP=1 -f /tmp/tests.sql
    Assert-NativeSuccess 'Database constraint tests failed.'
    Write-Output 'PASS: isolated database schema and constraints.'
} finally {
    if ($taskCreated) {
        & docker stop $taskContainer | Out-Null
    }
}
