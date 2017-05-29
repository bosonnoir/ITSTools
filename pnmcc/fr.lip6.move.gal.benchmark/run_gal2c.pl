#!/usr/bin/perl

# WARNING put the absolute path to the modelcheck executable here
my $spotmc = "/home/mcc/spot/tests/ltsmin/modelcheck";
my $techniques = "TECHNIQUES SEQUENTIAL_PROCESSING EXPLICIT STATE_COMPRESSION";

open IN, "< $ARGV[1]";

while (my $line = <IN>)
{
  if ($line =~ /^#property/)
  {
    my @words = split /\s+/,$line;
#    print "formula name is @words[1]\n";
    $line = <IN>;
    my $call = "$spotmc -Z $ARGV[0] '$line'";

    open RESULT, "($call) 2>&1 |" or die "An exception was raised when attempting to run "+$call+"\n";
    while (my $res = <RESULT>)
    {
      if ($res =~ /no accepting run found/)
      {
        print "FORMULA @words[1] TRUE $techniques\n";
      }
      if ($res =~ /an accepting run exists/)
      {
        print "FORMULA @words[1] FALSE $techniques\n";
      }
    }
    close RESULT;
  }
}

close IN;

