byte id=0;
byte t[3];


init { 
 d_step { 
t[0] =255; t[1] =255; t[2] =255; }
atomic { 
run Timer();
run P_0();
run P_1();
run P_2();
} }

proctype Timer() { 

q: if
::  d_step {t[0]!=0 && t[1]!=0 && t[2]!=0;t[0] = (t[0]-1)|((t[0]==255)*255);t[1] = (t[1]-1)|((t[1]==255)*255);t[2] = (t[2]-1)|((t[2]==255)*255);}  goto q; 

fi;
}

proctype P_0() { 

NCS: if
::  d_step {id==0;t[0] = 2;}  goto try; 

fi;
try: if
::  d_step {t[0] = 3;id = 0+1;}  goto wait; 

fi;
wait: if
::  d_step {t[0]==0;t[0] = 255;}  goto wait; 

:: t[0]==255 && id==0+1; goto CS; 

:: id!=0+1 && t[0]==255; goto NCS; 

fi;
CS: if
:: id = 0; goto NCS; 

fi;
}

proctype P_1() { 

NCS: if
::  d_step {id==0;t[1] = 2;}  goto try; 

fi;
try: if
::  d_step {t[1] = 3;id = 1+1;}  goto wait; 

fi;
wait: if
::  d_step {t[1]==0;t[1] = 255;}  goto wait; 

:: t[1]==255 && id==1+1; goto CS; 

:: id!=1+1 && t[1]==255; goto NCS; 

fi;
CS: if
:: id = 0; goto NCS; 

fi;
}

proctype P_2() { 

NCS: if
::  d_step {id==0;t[2] = 2;}  goto try; 

fi;
try: if
::  d_step {t[2] = 3;id = 2+1;}  goto wait; 

fi;
wait: if
::  d_step {t[2]==0;t[2] = 255;}  goto wait; 

:: t[2]==255 && id==2+1; goto CS; 

:: id!=2+1 && t[2]==255; goto NCS; 

fi;
CS: if
:: id = 0; goto NCS; 

fi;
}

