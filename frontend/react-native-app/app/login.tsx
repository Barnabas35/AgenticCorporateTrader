import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet } from 'react-native';
import { useNavigate } from 'react-router-dom';
import { useSessionToken, useUser } from '../components/userContext'; // Adjust the path as needed
import { getAuth, signInWithEmailAndPassword, GoogleAuthProvider, signInWithPopup } from 'firebase/auth';
import firebaseApp from '../components/firebase'; // Adjust the path as needed

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState(''); // State to display error messages
  const navigate = useNavigate();
  const { setUsername, setEmail: setUserEmail, setSessionToken, setProfileIconUrl } = useUser(); // Get the setters from context

  const auth = getAuth(firebaseApp);
  const googleProvider = new GoogleAuthProvider();

  useEffect(() => {
    // Load the Google API script when the component mounts
    const loadGoogleScript = () => {
      const script = document.createElement('script');
      script.src = 'https://apis.google.com/js/platform.js';
      script.async = true;
      script.onload = initializeGoogleSignIn;
      document.body.appendChild(script);
    };

    const initializeGoogleSignIn = () => {
      (window as any).gapi.load('auth2', () => {
        (window as any).gapi.auth2.init({
          client_id: 'AIzaSyDpIaiuRrQcE8Get-eXXhOI8R_Unr7GawQ',
        });
      });
    };

    loadGoogleScript();
  }, []);

  const handleGoogleLogin = async () => {
    try {
      const result = await signInWithPopup(auth, googleProvider);
      const credential = GoogleAuthProvider.credentialFromResult(result);
      const token = credential?.accessToken;
      const user = result.user;

      console.log('Google User:', user);
      console.log('Google Access Token:', token);

      // Here you can send the user data to your backend server for validation and login
      navigate('/');
    } catch (error) {
      console.error('Error during Google Sign-In:', error);
      window.alert('An unexpected error occurred during Google Sign-In. Please try again.');
    }
  };

  const isValidEmail = (email: string) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // Simple email validation regex
    return emailRegex.test(email);
  };

  const handleLogin = async () => {
    // Reset the error message on every login attempt
    setErrorMessage('');

    // Check if the email is in valid format
    if (!isValidEmail(email.trim())) {
      window.alert('Invalid Email: Please enter a valid email address.');
      return; // Prevent login if email format is invalid
    }

    if (!email || !password) {
      window.alert('Missing Fields: Please fill out both email and password.');
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email: email.trim(), password: password.trim() }),
      });

      const data = await response.json();

      if (data.status === 'Success' && data.session_token) {
        setSessionToken(data.session_token);

        // Navigate to the home page or any other protected route
        navigate('/');
      } else {
        window.alert(data.message || 'Login failed. Please check your credentials and try again.');
      }
    } catch (error) {
      console.error('Error during login:', error);
      window.alert('An unexpected error occurred. Please try again.');
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Login</Text>
      <TextInput
        style={styles.input}
        placeholder="Email"
        placeholderTextColor="#aaa"
        value={email}
        onChangeText={setEmail}
        keyboardType="email-address"
      />
      <TextInput
        style={styles.input}
        placeholder="Password"
        placeholderTextColor="#aaa"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
      />
      <TouchableOpacity style={styles.button} onPress={handleLogin}>
        <Text style={styles.buttonText}>Login</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.googleButton} onPress={handleGoogleLogin}>
        <Text style={styles.buttonText}>Login with Google</Text>
      </TouchableOpacity>
      {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}
      <TouchableOpacity onPress={() => navigate('/register')} style={styles.registerLink}>
        <Text style={styles.registerText}>Don't have an account? Register here</Text>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center', // Center horizontally
    width: '100%',
    padding: 20,
    backgroundColor: '#f0f0f0', // Background color of the login screen
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  input: {
    width: '30%',
    height: 50,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 10,
    marginBottom: 15,
  },
  button: {
    width: '30%',
    height: 50,
    backgroundColor: '#4CAF50', // Green background for the button
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    marginBottom: 15,
  },
  googleButton: {
    width: '30%',
    height: 50,
    backgroundColor: '#DB4437', // Red background for Google button
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    marginBottom: 15,
  },
  buttonText: {
    color: 'white',
    fontSize: 18,
  },
  errorText: {
    color: 'red',
    marginTop: 10,
  },
  registerLink: {
    marginTop: 20,
  },
  registerText: {
    color: '#4CAF50', // Color for the register link
    fontSize: 16,
  },
});

export default Login;
