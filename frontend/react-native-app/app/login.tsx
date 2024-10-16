// src/app/Login.tsx

import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../components/userContext'; // Adjust the path as needed

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();
  const { setUsername, setEmail: setUserEmail, setSessionToken, setProfileIconUrl } = useUser(); // Get the setters from context

  const handleLogin = async () => {
    const url = 'https://tradeagently.dev/login';
    const bodyData = {
      email: email.trim(),
      password: password.trim(),
    };

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(bodyData),
      });

      const data = await response.json();

      if (data.session_token != null) {
        // Store the session token in context
        setSessionToken(data.session_token);

        // Fetch username with the session token
        const usernameResponse = await fetch('https://tradeagently.dev/get-username', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ session_token: data.session_token }),
        });

        const usernameData = await usernameResponse.json();

        if (usernameData.status === 'Success') {
          console.log('Username:', usernameData.username);
          // Store the username in context
          setUsername(usernameData.username);
        } else {
          Alert.alert('Error', usernameData.message || 'Failed to fetch username.');
        }

        // Fetch email with the session token
        const emailResponse = await fetch('https://tradeagently.dev/get-email', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ session_token: data.session_token }),
        });

        const emailData = await emailResponse.json();

        if (emailData.status === 'Success') {
          setUserEmail(emailData.email);
        } else {
          Alert.alert('Error', emailData.message || 'Failed to fetch email.');
        }

        // Fetch profile icon with the session token
        const profileIconResponse = await fetch('https://tradeagently.dev/get-profile-icon', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ session_token: data.session_token }),
        });

        const profileIconData = await profileIconResponse.json();

        if (profileIconData.status === 'Success') {
          setProfileIconUrl(profileIconData.profile_icon_url);
        } else {
          Alert.alert('Error', profileIconData.message || 'Failed to fetch profile icon.');
        }

        // Navigate to the home page or any other protected route
        navigate('/');
      } else {
        // Handle errors (e.g., invalid credentials)
        Alert.alert('Error', data.message || 'Login failed. Please try again.');
      }
    } catch (error) {
      console.error('Error during login:', error);
      Alert.alert('Error', 'An unexpected error occurred. Please try again.');
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
  },
  buttonText: {
    color: 'white',
    fontSize: 18,
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
