import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { useNavigate } from 'react-router-dom';
import { useSessionToken, useUser } from '../components/userContext';
import { getAuth, signInWithPopup, GoogleAuthProvider } from 'firebase/auth';
import firebaseApp from '../components/firebase';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();
  const { setSessionToken, setUsername, setEmail: setUserEmail } = useUser();

  const auth = getAuth(firebaseApp);
  const googleProvider = new GoogleAuthProvider();

  const handleGoogleLogin = async () => {
    try {
      const result = await signInWithPopup(auth, googleProvider);
      const user = result.user;

      if (!user) {
        throw new Error('No user information returned from Google Sign-In.');
      }

      // Retrieve ID token from the current user
      const idToken = await user.getIdToken(true); // Force refresh to ensure a valid token
      console.log('Google User ID Token:', idToken);

      // Call the backend to exchange tokens
      const response = await fetch('https://tradeagently.dev/exchange-tokens', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ auth_token: idToken }),
      });

      const data = await response.json();
      console.log('Exchange Tokens Response:', data);

      if (data.status === 'Success' && data.session_token) {
        setSessionToken(data.session_token);
        setUsername(user.displayName || 'User');
        setUserEmail(user.email || '');
        navigate('/'); // Redirect to the home page
      } else if (data.status === 'Success: Register User') {
        Alert.alert('New User', 'Please complete registration.');
        navigate('/register');
      } else {
        throw new Error(data.message || 'Failed to exchange tokens.');
      }
    } catch (error) {
      console.error('Error during Google Login:', error);
      Alert.alert('Login Failed', 'An error occurred during Google Login. Please try again.');
    }
  };

  const handleLogin = async () => {
    setErrorMessage('');

    if (!email || !password) {
      Alert.alert('Missing Fields', 'Please fill out both email and password.');
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();
      if (data.status === 'Success' && data.session_token) {
        setSessionToken(data.session_token);
        navigate('/');
      } else {
        Alert.alert('Login Failed', data.message || 'Invalid credentials.');
      }
    } catch (error) {
      console.error('Login Error:', error);
      Alert.alert('Error', 'An error occurred during login. Please try again.');
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
    alignItems: 'center',
    width: '100%',
    padding: 20,
    backgroundColor: '#f0f0f0',
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
    backgroundColor: '#4CAF50',
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    marginBottom: 15,
  },
  googleButton: {
    width: '30%',
    height: 50,
    backgroundColor: '#DB4437',
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
    color: '#4CAF50',
    fontSize: 16,
  },
});

export default Login;
