from firebase import firebase
#https://pypi.python.org/pypi/python-firebase/1.2

firebase = firebase.FirebaseApplication('https://smart-fridge-76b1c.firebaseio.com', None)
serials = firebase.get('/Serials', None)

print serials

while True:
    print "Scan a serial: ",
    scanned = raw_input().decode('utf-8')
    if scanned in serials:
        print "You scanned " + serials[scanned]
    else:
        print "I don't recognize that product..."