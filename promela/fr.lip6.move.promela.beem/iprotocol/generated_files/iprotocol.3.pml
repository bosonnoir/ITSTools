
chan Get =[0] of {int};
chan Put =[0] of {int};
chan SAck =[0] of {int};
chan SNak =[0] of {int};
chan SData =[0] of {int};
chan RAck =[0] of {int};
chan RNak =[0] of {int};
chan RData =[0] of {int};
chan RCorrData =[0] of {int};
chan Timeout =[0] of {int};

active proctype Timer() { 

tick: if
:: Timeout!0; goto tick; 

fi;
}

active proctype Producer() { 
byte message=0;

wait: if
::  goto produce; 

fi;
produce: if
::  atomic {Get!message;message = (message+1)%6;}  goto wait; 

fi;
}

active proctype Consumer() { 
byte message=0;

wait: if
:: Put?message; goto consume; 

fi;
consume: if
::  goto wait; 

fi;
}

active proctype Medium() { 
byte value=0;

wait: if
:: SData?value; goto data; 

:: RAck?value; goto ack; 

:: RNak?value; goto nak; 

fi;
data: if
:: RData!value; goto dataOk; 

:: RCorrData!value; goto wait; 

::  goto wait; 

fi;
ack: if
:: SAck!value; goto ackOk; 

::  goto wait; 

fi;
nak: if
:: SNak!value; goto nakOk; 

::  goto wait; 

fi;
dataOk: if
::  goto wait; 

fi;
ackOk: if
::  goto wait; 

fi;
nakOk: if
::  goto wait; 

fi;
}

active proctype Sender() { 
byte sendseq=1;
byte rack=0;
byte value=0;

wait: if
:: SAck?value; goto ack; 

:: SNak?value; goto nak; 

:: Timeout?0; goto timeisout; 

::  atomic {(rack+3)%6>sendseq;Get?value;}  goto data; 

fi;
data: if
::  atomic {SData!sendseq;sendseq = (sendseq+1)%6;}  goto wait; 

fi;
ack: if
::  d_step {(rack<sendseq && rack<value && value<sendseq) || (rack>sendseq && sendseq<value && value<rack);rack = value;}  goto wait; 

:: (rack>=sendseq || rack>=value || value>=sendseq) && (rack<=sendseq || sendseq>=value || value>=rack); goto wait; 

fi;
nak: if
::  atomic {(rack<sendseq && rack<value && value<sendseq) || (rack>sendseq && sendseq<value && value<rack);SData!value;}  goto wait; 

:: (rack>=sendseq || rack>=value || value>=sendseq) && (rack<=sendseq || sendseq>=value || value>=rack); goto wait; 

fi;
timeisout: if
::  atomic {(rack+1)%6!=sendseq;SData!(rack+1)%6;}  goto wait; 

:: (rack+1)%6==sendseq; goto wait; 

fi;
}

active proctype Receiver() { 
byte i=0;
byte value=0;
byte sent=0;
byte recseq=0;
byte lack=0;
byte recbuf[6];
byte nakd[6];

wait: if
:: RData?value; goto data; 

:: RCorrData?value; goto corr_data; 

::  atomic {Timeout?0;i = 0;}  goto on_timeisout; 

fi;
data: if
::  d_step {value!=(recseq+1)%6;recbuf[value] = 1;i = (recseq+1)%6;}  goto send_naks; 

::  atomic {value==(recseq+1)%6;Put!value;recseq = (recseq+1)%6;sent = (sent+1)%6;}  goto put_data; 

fi;
put_data: if
::  atomic {sent==3/2;RAck!recseq;lack = recseq;sent = 0;}  goto put_data; 

::  atomic {sent!=3/2 && recbuf[(recseq+1)%6]==1;Put!(recseq+1)%6;recseq = (recseq+1)%6;recbuf[recseq] = 0;}  goto put_data; 

:: sent!=3/2 && recbuf[(recseq+1)%6]==0; goto wait; 

fi;
send_naks: if
::  d_step {i!=value && nakd[i]==1;i = (i+1)%6;}  goto send_naks; 

::  atomic {i!=value && nakd[i]==0;RNak!i;nakd[i] = 1;i = (i+1)%6;}  goto send_naks; 

:: i==value; goto wait; 

fi;
corr_data: if
::  atomic {nakd[value]==0;RNak!value;}  goto wait; 

:: nakd[value]==1; goto wait; 

fi;
on_timeisout: if
::  d_step {i<6;nakd[i] = 0;i = i+1;}  goto on_timeisout; 

::  atomic {i==6;RNak!(recseq+1)%6;nakd[(recseq+1)%6] = 1;}  goto timeisout_ack; 

fi;
timeisout_ack: if
:: RAck!lack; goto wait; 

fi;
}

