# Get the API key from user input
# $apiKey = "AIzaSyD3nZncaJyM045MWglg3HwxvOTHypWqvro"
$apiKey = "AIzaSyDhZZ2nGTklMRJqcQKkNcd2DWofXUVSVrs"

# Set it as an environment variable
[System.Environment]::SetEnvironmentVariable('GEMINI_API_KEY', $apiKey, [System.EnvironmentVariableTarget]::User)

Write-Host "API key has been set. Please restart your application for the changes to take effect."
Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')