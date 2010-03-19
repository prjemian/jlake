#!/usr/bin/tclsh

set inFile test.smr
set outFile $inFile.xml

set f [open $inFile r]
set buf [read $f]
close $f

proc element {tag unit value} {
  return [format "<%s unit=\"%s\">%s</%s>" $tag $unit $value $tag]
}

set output ""
foreach {Q I dI} [split $buf] {
  if {[string length $Q]} {
    set    line [element "Q"  "1/A"  $Q]
    append line [element "I"  "1/cm" $I]
    append line [element "Idev" "1/cm" $dI]
	append output "      "
	append output <Idata>$line</Idata>\n
  }
}
set xml "    <SASdata name=\"slit-smeared\">\n"
append xml $output
append xml "    </SASdata>"

set f [open $outFile w]
puts $f $xml
close $f
