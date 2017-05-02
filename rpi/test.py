from firebase import Firebase
from getpass import getpass
#https://pypi.python.org/pypi/python-firebase/1.2

firebase = Firebase('https://smart-fridge-76b1c.firebaseio.com/Serials')
serials = firebase.get()

while True:
    scanned = getpass("").decode('utf-8')
    if scanned in serials:
        print "You scanned " + serials[scanned]
    else:
        print "I don't recognize that product..."
