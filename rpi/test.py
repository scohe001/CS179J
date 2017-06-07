import time
import getpass
import sys
import os
import fcntl
import serial
#import pyttsx
from firebase import Firebase
#https://pypi.python.org/pypi/python-firebase/1.2

#So that program won't stop on a call for stdin
fl = fcntl.fcntl(sys.stdin.fileno(), fcntl.F_GETFL)
fcntl.fcntl(sys.stdin.fileno(), fcntl.F_SETFL, fl | os.O_NONBLOCK)

#setup serial for communicating with the Arduino
ser = serial.Serial('/dev/ttyACM0', 9600)

#engine = pyttsx.init()
fire_url = 'https://smart-fridge-76b1c.firebaseio.com'

fireserial = Firebase(fire_url+'/Serials')
firein = Firebase(fire_url+'/In Fridge')
#fireout = Firebase(fire_url+'/Taken Out')
fireputin = Firebase(fire_url+'/Functions/PutIn')
firetakeout = Firebase(fire_url+'/Functions/TakeOut')

serials = fireserial.get()
num_in = len(firein.get())

def put_in(scanned):
    global num_in

    num_in += 1
    #fireputin.patch({scanned:True})
    print "Puttin in " + serials[scanned]
    
def take_out(scanned):
    global num_in

    num_in -= 1

    #firetakeout.patch({scanned:True})
    print "Taking out " + serials[scanned] 
    
running = True
def get_serial():
    global serials, num_in
    global running
    
    #print serials
    
    last_scan = -1
    last_in = -1
    while running:
        #try to get some data from the Arduino
        bytesToRead = ser.inWaiting()
        if(bytesToRead > 0):
            val = ser.readline()
            print "Got something!\t" + val,

            if(val[0] == "1"):
                last_in = 0
                if last_scan >= 0:
                    print "Matched putting in"
                    (last_in, last_scan) = (-1, -1)
            elif(val[0] == "2"):
                ser.write(str(num_in))
            

        #Try to read a scanned serial number
        try:
            all_scanned = sys.stdin.read().decode('utf-8')
            for scanned in all_scanned.split('\n'):
                if scanned == '': break
                if scanned == 'stop'.decode('utf-8'): running = False
                elif scanned in serials:
                    print "Scanned " + serials[scanned]
                    
                    if last_in >= 0:
                        take_out(scanned)
                        print "Matched taking out"
                        (last_in, last_scan) = (-1, -1) 
                    else: 
                        put_in(scanned)
                        last_scan = 0
                    
                else:
                    print "I don't recognize that product..."
        except IOError:
            pass

        if(last_scan >= 0):
            last_scan += 1
            if(last_scan > 500): last_scan = -1
        if(last_in >= 0):
            last_in += 1
            if(last_in > 500): last_in = -1
            
        time.sleep(.01)

            

print "Ready!"
get_serial()
