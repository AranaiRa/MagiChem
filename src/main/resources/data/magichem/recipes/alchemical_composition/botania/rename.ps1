# You need to open a PS terminal and run the script from there. 
# Navigate to the directory if necessary and then run these two commands in order.
#
# Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
# .\recursive_string_replace.ps1

$TargetDir = (Get-Location).ToString()
$LogFile = $TargetDir+"\rsr.log"

$TargetText = "stone_bricks"
$Replacement = "bricks"

Function Log-Message([String]$Message)
{
    Add-Content $LogFile $Message
}
 
"Beginning replacement process." | Out-File -FilePath $LogFile

foreach($file in (gci $TargetDir -recurse -include "*.json")){
    if($file|select-string $TargetText -simplematch -quiet){
        "Updating lines in $($file.fullname)" |out-file $LogFile -append
        $file|select-string $TargetText -simplematch -allmatches|select -expandproperty linenumber -unique|out-file $LogFile -append
        (gc $file.fullname) | %{$_ -replace [regex]::escape($TargetText),$Replacement} | set-content $file.fullname
		Get-Item $file.Fullname | rename-item -newname {$_.name -replace $TargetText,$Replacement}
    }
}

Log-Message("Replacement completed.")