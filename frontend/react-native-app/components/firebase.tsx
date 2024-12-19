// firebase.ts
import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider } from 'firebase/auth';

// Firebase configuration for Agentic Corporate Trader
const firebaseConfig = {
    apiKey: "AIzaSyDpIaiuRrQcE8Get-eXXhOI8R_Unr7GawQ", 
    authDomain: "agenticcorporatetrader.firebaseapp.com",
    projectId: "agenticcorporatetrader",
    messagingSenderId: "68009005920",
    appId: "1:68009005920:web:65105c6c427b402ae60405"
  };
// Initialize Firebase app
const firebaseApp = initializeApp(firebaseConfig);

// Firebase Authentication instance
export const auth = getAuth(firebaseApp);

// Google Auth Provider for Sign-In
export const googleProvider = new GoogleAuthProvider();

export default firebaseApp;
