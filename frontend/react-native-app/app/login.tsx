import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, Image } from 'react-native';
import { useNavigate } from 'react-router-dom';
import { useSessionToken, useUser } from '../components/userContext';
import { getAuth, signInWithPopup, GoogleAuthProvider } from 'firebase/auth';
import firebaseApp from '../components/firebase';

// Basic email regex
function validateEmail(email: string): boolean {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email.toLowerCase());
}

// Path to your Google icon asset
const googleIcon = require('../assets/images/google-icon.png');

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // Separate error states
  const [emailError, setEmailError] = useState('');
  const [loginError, setLoginError] = useState('');

  const navigate = useNavigate();
  const { setSessionToken, setUsername, setEmail: setUserEmail } = useUser();

  const auth = getAuth(firebaseApp);
  const googleProvider = new GoogleAuthProvider();

  const handleGoogleLogin = async () => {
    try {
      const result = await signInWithPopup(auth, googleProvider);
      const user = result.user;
      if (!user) throw new Error('No user information returned from Google Sign-In.');

      const idToken = await user.getIdToken(true);
      const response = await fetch('https://tradeagently.dev/exchange-tokens', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ auth_token: idToken }),
      });

      const data = await response.json();
      if (data.status === 'Success' && data.session_token) {
        setSessionToken(data.session_token);
        setUsername(user.displayName || 'User');
        setUserEmail(user.email || '');
        navigate('/');
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
    // Clear previous errors
    setEmailError('');
    setLoginError('');

    // Validate presence
    if (!email || !password) {
      Alert.alert('Missing Fields', 'Please fill out both email and password.');
      return;
    }

    // Validate email format
    if (!validateEmail(email)) {
      setEmailError('Please enter a valid email address.');
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();

      // Check for session token
      if (data.status === 'Success' && data.session_token) {
        setSessionToken(data.session_token);
        navigate('/');
      } else {
        // Show a styled error box
        setLoginError('Invalid login credentials. Please try again.');
      }
    } catch (error) {
      console.error('Login Error:', error);
      Alert.alert('Error', 'An error occurred during login. Please try again.');
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Login</Text>

      {/* Email Error Info (above the email input) */}
      {emailError ? (
        <View style={styles.infoBox}>
          <Text style={styles.infoBoxText}>{emailError}</Text>
        </View>
      ) : null}

      <TextInput
        style={styles.input}
        placeholder="Email"
        placeholderTextColor="#aaa"
        value={email}
        onChangeText={(val) => {
          setEmail(val);
          if (emailError) setEmailError(''); // clear as user types
        }}
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

      {/* Google Login Button with Icon */}
      <TouchableOpacity style={styles.googleButton} onPress={handleGoogleLogin}>
        <View style={styles.googleButtonContent}>
          <Image source={googleIcon} style={styles.googleIcon} />
          <Text style={styles.buttonText}>Login with Google</Text>
        </View>
      </TouchableOpacity>

      {/* Nicer "Error Box" for invalid credentials */}
      {loginError ? (
        <View style={styles.errorBox}>
          <Text style={styles.errorBoxText}>{loginError}</Text>
        </View>
      ) : null}

      <TouchableOpacity onPress={() => navigate('/register')} style={styles.registerLink}>
        <Text style={styles.registerText}>Don't have an account? Register here</Text>
      </TouchableOpacity>
    </View>
  );
};

// ----------------
// Styles
// ----------------
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

  // Info box for email errors
  infoBox: {
    width: '30%',
    backgroundColor: '#fff4e5',
    borderColor: '#ffca99',
    borderWidth: 1,
    borderRadius: 8,
    padding: 8,
    marginBottom: 8,
  },
  infoBoxText: {
    color: '#b45b00',
    fontSize: 14,
  },

  input: {
    width: '30%',
    height: 50,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 10,
    marginBottom: 15,
    backgroundColor: '#fff',
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
    borderRadius: 8,
    marginBottom: 15,
    justifyContent: 'center',
    alignItems: 'center',
  },
  googleButtonContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  googleIcon: {
    width: 24,
    height: 24,
    marginRight: 8,
  },
  buttonText: {
    color: 'white',
    fontSize: 18,
  },

  // Nicer error box for login credentials
  errorBox: {
    width: '30%',
    backgroundColor: '#f8d7da',
    borderColor: '#f5c6cb',
    borderWidth: 1,
    borderRadius: 8,
    padding: 10,
    marginTop: 10,
  },
  errorBoxText: {
    color: '#721c24',
    fontSize: 14,
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
