// firebase.ts
import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';

const firebaseConfig = {
    apiKey: "AIzaSyDpIaiuRrQcE8Get-eXXhOI8R_Unr7GawQ",
    authDomain: "agenticcorporatetrader.firebaseapp.com",
    databaseURL: "https://agenticcorporatetrader-default-rtdb.europe-west1.firebasedatabase.app",
    projectId: "agenticcorporatetrader",
    storageBucket: "agenticcorporatetrader.appspot.com",
    messagingSenderId: "68009005920",
    appId: "1:68009005920:web:65105c6c427b402ae60405",
    measurementId: "G-3QTNN5TQFM"
};

// Initialize Firebase
const firebaseApp = initializeApp(firebaseConfig);

// Export the authentication service instance
export const auth = getAuth(firebaseApp);
export default firebaseApp;
