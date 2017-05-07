import time
import getpass
import sys
import os
import fcntl
import datetime
#import pyttsx
from firebase import Firebase
#https://pypi.python.org/pypi/python-firebase/1.2

#So that program won't stop on a call for stdin
fl = fcntl.fcntl(sys.stdin.fileno(), fcntl.F_GETFL)
fcntl.fcntl(sys.stdin.fileno(), fcntl.F_SETFL, fl | os.O_NONBLOCK)

#engine = pyttsx.init()
fire_url = 'https://smart-fridge-76b1c.firebaseio.com'

fireserial = Firebase(fire_url+'/Serials')
firein = Firebase(fire_url+'/In Fridge')
fireout = Firebase(fire_url+'/Taken Out')

serials = fireserial.get()
infridge = firein.get()
outfridge = fireout.get()

running = True
def get_serial():
    global serials, infridge, outfridge
    global fireserial, firein, fireout
    global running
    
    print serials
    
    a = 0 #counter for updating
    while running:
        
        try:
            scanned = sys.stdin.read()[:-1].decode('utf-8')
            if scanned == 'stop'.decode('utf-8'): running = False
            elif scanned in serials:
                print "Scanned " + serials[scanned]
                #engine.say(str("Test test test"))
                #engine.runAndWait()
                
                if scanned in outfridge:
                    Firebase(fire_url+'/Taken Out/'+scanned).delete()
                    Firebase(fire_url+'/In Fridge/').patch({scanned:True})
                    infridge[scanned] = outfridge[scanned]
                    outfridge.pop(scanned)
                    print "Putting it back in..."
                elif scanned in infridge: 
                    Firebase(fire_url+'/In Fridge/'+scanned).delete()
                    Firebase(fire_url+'/Taken Out/').patch({scanned:time.strftime("%b %d %Y %I:%M%p")})
                    outfridge[scanned] = infridge[scanned]
                    infridge.pop(scanned)
                    print "Taking it out..."
                else: 
                    Firebase(fire_url+'/In Fridge/').patch({scanned:True})
                    print "Ooh, new purchase!"
                
            else:
                print "I don't recognize that product..."
        except IOError:
            pass
        
        if a >= 50:
            a = 0
            #Check if the database was updated
            serials = fireserial.get()
            infridge = firein.get()
            outfridge = fireout.get()
            
            #Check for things out too long
            for key, val in outfridge.items():
                if key == 'na': continue
                outTime = datetime.datetime.strptime(val, '%b %d %Y %I:%M%p')
                if (datetime.datetime.now() - outTime).total_seconds() > 60:
                    print "old!"
                    Firebase(fire_url+'/Taken Out/'+key).delete()
                    outfridge.pop(key)

        a += 1

            

get_serial()