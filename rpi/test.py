import threading
import time
import getpass
from firebase import Firebase
#https://pypi.python.org/pypi/python-firebase/1.2


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
        
        time.sleep(.5)

def get_serial():
    global running
    
    while running:
        scanned = getpass.getpass('').decode('utf-8')
        if scanned == 'stop'.decode('utf-8'): running = False
        elif scanned in serials:
            print "You scanned " + serials[scanned]
            
            if scanned in outfridge:
                Firebase(fire_url+'/Taken Out/'+scanned).delete()
                Firebase(fire_url+'/In Fridge/').patch({scanned:True})
                print "Putting it back in..."
            elif scanned in infridge: 
                Firebase(fire_url+'/In Fridge/'+scanned).delete()
                Firebase(fire_url+'/Taken Out/').patch({scanned:True})
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
