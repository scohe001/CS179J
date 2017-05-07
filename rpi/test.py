import threading
import time
import getpass
import pyttsx
from firebase import Firebase
#https://pypi.python.org/pypi/python-firebase/1.2

engine = pyttsx.init()
fire_url = 'https://smart-fridge-76b1c.firebaseio.com'

fireserial = Firebase(fire_url+'/Serials')
firein = Firebase(fire_url+'/In Fridge')
fireout = Firebase(fire_url+'/Taken Out')

serials = fireserial.get()
infridge = firein.get()
outfridge = fireout.get()


threads = {}
running = True

def update_db():
    global serials, infridge, outfridge
    global fireserial, firein, fireout
    
    while running:
        serials = fireserial.get()
        infridge = firein.get()
        outfridge = fireout.get()
        
        time.sleep(15)


def get_serial():
    global serials, infridge, outfridge
    global fireserial, firein, fireout
    global running
    
    while running:
        scanned = getpass.getpass('').decode('utf-8')
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
                Firebase(fire_url+'/Taken Out/').patch({scanned:True})
                outfridge[scanned] = infridge[scanned]
                infridge.pop(scanned)
                print "Taking it out..."
            else: 
                Firebase(fire_url+'/In Fridge/').patch({scanned:True})
                print "Ooh, new purchase!"
            
        else:
            print "I don't recognize that product..."
            

threads['getter'] = threading.Thread(name='getter', target=get_serial)
threads['updater'] = threading.Thread(name='updater', target=update_db)

threads['getter'].start()
threads['updater'].start()
