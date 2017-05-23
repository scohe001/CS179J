var functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

//Quick mockup for testing db reading/writing with https requests
//If the value 1 is in db, write 0. Otherwise write 1
exports.requestTest = functions.https.onRequest((req, res) => {
      var test = 0;
      admin.database().ref('Test').once("value", function(data) {
        console.log("Value at Test is: ", data.val());
        test = data.val();
        
        if(test == 0) {
          admin.database().ref('Test').set(1);
          res.status(200).send("Worked! New value should be 1");
        } else {
          admin.database().ref('Test').set(0);
          res.status(200).send("Worked! New value should be 0");
        }
      });
      
});

//Check if anything has been out of the fridge for longer than a set period
//If it has, remove it from the db (still exists in history)
exports.checkExpired = functions.https.onRequest((req, res) => {
    admin.database().ref('Taken Out').once("value", function(data) {
      var takenout = data.val();
      
      //Check the time difference for all items in Taken Out
      for(var key in takenout){
        if(takenout[key] == 'na') continue;
        var outDate = new Date(takenout[key]["outDate"]);
        if(new Date() - outDate > (60*1000*5)) { //5 minutes
          console.log("Killing: Taken Out/"+key);
          admin.database().ref("Taken Out/"+key).set(null);
        }
      }
      
      res.status(200).send("Success.");
    });
});


//Listen for food items added to inFridge to properly format them
exports.putIn = functions.database.ref('Functions/PutIn/{Serial}').onWrite(event=> {
  // Make sure we don't do anything when we call ourselves when we remove the data
  if (event.data.previous.exists()) {
    return;
  }
  
  const serial = event.params.Serial;
  console.log("Found a new item with serial: ", event.params.Serial);
  
  console.log("Killing Functions/PutIn/" + serial);
  admin.database().ref('Functions/PutIn/'+serial).set(null);
  
  //Read the values out of the fridge
  admin.database().ref('Taken Out/').once("value", function(data) {
    var outfridge = data.val();
    var matches = [];
    
    //Find all items out of the fridge with our serial
    for(var key in outfridge) {
      if(outfridge[key] == 'na') continue;
      if(outfridge[key]["Serial"] == serial) matches.push(key);
    }
    
    //If no matches, this is a new item
    if(matches.length == 0) {
      console.log("New Fridge item. Adding to In Fridge at "+new Date());
      admin.database().ref('History').push().set({
        inDate: (new Date()).toISOString(),
        Serial: serial
      });
      return admin.database().ref('In Fridge').push().set({
        inDate: (new Date()).toISOString(),
        Serial: serial
      });
    }
    
    //Else, find the one out of fridge the longest to return
    var biggestDiff = 0;
    var smallestKey = matches[0];
    for (var i = 0; i < matches.length; i++) {
       if(new Date() - new Date(outfridge[matches[i]]["outDate"]) > biggestDiff) smallestKey = matches[i];
    }
    
    //Remove from out of fridge and move to In Fridge
    console.log("Killing Taken Out/"+smallestKey);
    admin.database().ref("Taken Out/"+smallestKey).set(null);
    return admin.database().ref("In Fridge/").push().set({
      inDate: outfridge[smallestKey]["inDate"],
      Serial: outfridge[smallestKey]["Serial"]
    });
    
  });
  
});

//Listen for food items to be removed from fridge
exports.takeOut = functions.database.ref('Functions/TakeOut/{Serial}').onWrite(event=> {
  // Make sure we don't do anything when we call ourselves when we remove the data
  if (event.data.previous.exists()) {
    return;
  }
  
  const serial = event.params.Serial;    
  console.log("Killing Functions/Takeout/"+serial);
  admin.database().ref('Functions/TakeOut/'+serial).set(null);
  
  //Read the values in the fridge
  admin.database().ref('In Fridge').once("value", function(data) {
    var infridge = data.val();
    var matches = [];
    
    //Find all items in the fridge with our serial
    for(var key in infridge) {
      if(infridge[key] == 'na') continue;
      if(infridge[key]["Serial"] == serial) matches.push(key);
    }
    console.log("Found these matches: ", matches);
    
    //If there is no such item
    if(matches.length == 0) {
      console.log("NO MATCH FOUND");
      return;
    }
    
    //Determine which is the oldest to pull
    var biggestDiff = 0;
    var smallestKey = matches[0];
    for (var i = 0; i < matches.length; i++) {
      if(new Date() - new Date(infridge[matches[i]]["inDate"]) > biggestDiff) smallestKey = matches[i];
    }
    
    //Take out of the fridge and put in Taken Out
    console.log("Killing In Fridge/"+smallestKey);
    admin.database().ref("In Fridge/"+smallestKey).set(null);
    return admin.database().ref("Taken Out/").push().set({
      inDate: infridge[smallestKey]["inDate"],
      Serial: infridge[smallestKey]["Serial"],
      outDate: (new Date()).toISOString()
    });
    

    
  });
  
});
