param(
  [ValidateSet('valid','bad-signature','expired')][string]$Mode='valid',
  [string]$EventId="evt-$([DateTimeOffset]::UtcNow.ToUnixTimeSeconds())",
  [string]$Secret=$env:WEBHOOK_SECRET,
  [switch]$Duplicate
)
$ErrorActionPreference='Stop'
if([string]::IsNullOrWhiteSpace($Secret)){ $Secret='replace-with-a-long-random-local-secret' }
$timestamp=[DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
if($Mode -eq 'expired'){ $timestamp-=600 }
$body='{"type":"invoice.paid","amount":4900,"currency":"USD"}'
$payload="$timestamp.$body"
$hmac=[System.Security.Cryptography.HMACSHA256]::new([Text.Encoding]::UTF8.GetBytes($Secret))
$digest=[Convert]::ToHexString($hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($payload))).ToLowerInvariant()
$signature=if($Mode -eq 'bad-signature'){'sha256=deadbeef'}else{"sha256=$digest"}
$headers=@{'X-Event-Id'=$EventId;'X-Timestamp'="$timestamp";'X-Signature-256'=$signature}
Invoke-RestMethod -Method Post -Uri 'http://localhost:8082/webhook/receive' -Headers $headers -ContentType 'application/json' -Body $body
if($Duplicate){ Invoke-RestMethod -Method Post -Uri 'http://localhost:8082/webhook/receive' -Headers $headers -ContentType 'application/json' -Body $body }
