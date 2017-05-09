import time
import getpass
import sys
import os
import fcntl
import datetime
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
fireout = Firebase(fire_url+'/Taken Out')

serials = fireserial.get()
infridge = firein.get()
outfridge = fireout.get()

def put_in(scanned):
    global infridge, outfridge

    if scanned in outfridge:    
        Firebase(fire_url+'/Taken Out/'+scanned).delete()
        outfridge.pop(scanned)
        print "Puttin it back in..."
    if scanned in infridge:
        infridge[scanned]["num"] += 1
        Firebase(fire_url+'/In Fridge/').patch({scanned:{"num":infridge[scanned]["num"]}})
    else:        
        Firebase(fire_url+'/In Fridge/').patch({scanned:{"num":1}})
        infridge[scanned] = {}
        infridge[scanned]["num"] = 1

running = True
def get_serial():
    global serials, infridge, outfridge
    global fireserial, firein, fireout
    global running
    
    print serials
    
    last_update = datetime.datetime.now() #counter for updating
    while running:

        #Try to read a scanned serial number
        try:
            all_scanned = sys.stdin.read().decode('utf-8')
            for scanned in all_scanned.split('\n'):
                if scanned == '': break
                if scanned == 'stop'.decode('utf-8'): running = False
                elif scanned in serials:
                    print "Scanned " + serials[scanned]
                    #engine.say(str("Test test test"))
                    #engine.runAndWait()
                    
                    if scanned in infridge: 
                        #Firebase(fire_url+'/In Fridge/'+scanned).delete()
                        #Firebase(fire_url+'/Taken Out/').patch({scanned:time.strftime("%b %d %Y %I:%M%p")})
                        #outfridge[scanned] = infridge[scanned]
                        #infridge.pop(scanned)
                        print "Taking it out..."
                    #else: 
                    put_in(scanned)
                    
                else:
                    print "I don't recognize that product..."
        except IOError:
            pass

        #try to get some data from the Arduino
        bytesToRead = ser.inWaiting()
        if(bytesToRead > 0):
            val = ser.readline()
            print "Got something!\t" + val,
        
        if (datetime.datetime.now() - last_update).total_seconds() >= 30:
            last_update = datetime.datetime.now()
            #Check if the database was updated
            serials = fireserial.get()
            infridge = firein.get()
            outfridge = fireout.get()
            
            #Check for things out too long
            for key, val in outfridge.items():
                if key == 'na': continue
                outTime = datetime.datetime.strptime(val, '%b %d %Y %I:%M%p')
                #Made this 60 seconds for testing, should be 3600 in production
                if (datetime.datetime.now() - outTime).total_seconds() > 60:
                    print "old!"
                    Firebase(fire_url+'/Taken Out/'+key).delete()
                    outfridge.pop(key)

        time.sleep(.01)

            

get_serial()
