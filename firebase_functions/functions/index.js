'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.deleteUserDataOnUninstall = functions.analytics.event('app_remove').onLog(event => {
  	const uid = event.data.uid;
  	admin.database().ref('/users/${uid}').child('name').once("value", function(snapshot) {
  		console.log(snapshot.val());
  		if(snapshot.val() === "Anonymous"){
  			admin.auth().deleteUser(uid).then(() => {
	          console.log('Deleted user account', uid, 'because of inactivity');
	        }).catch(error => {
	          console.error('Deletion of inactive user account', uid, 'failed:', error);
	        });
  			return admin.database().ref('/users/${uid}').remove();
  		}
	});
});