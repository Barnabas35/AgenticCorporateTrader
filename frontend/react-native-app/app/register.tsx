import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Modal, Pressable } from 'react-native';
import { useNavigate } from 'react-router-dom';
import { getAuth, GoogleAuthProvider, signInWithPopup } from 'firebase/auth';
import firebaseApp from '../components/firebase';

const Register: React.FC = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [userType, setUserType] = useState('default'); // Default to "select user type"
  const [modalVisible, setModalVisible] = useState(false);
  const [modalMessage, setModalMessage] = useState('');
  const navigate = useNavigate();

  const auth = getAuth(firebaseApp);
  const googleProvider = new GoogleAuthProvider();

  const handleGoogleRegister = async () => {
    try {
      const result = await signInWithPopup(auth, googleProvider);
      const user = result.user;
  
      if (!user) {
        throw new Error("User not found after Google sign-in.");
      }
  
      // Retrieve the ID token for the authenticated user
      const idToken = await user.getIdToken(true); // Force refresh to ensure a valid token
      console.log("Google User ID Token:", idToken);
      console.log(userType)
      // Send the ID token and user type to the backend
      const url = 'https://tradeagently.dev/register-with-token';
      const bodyData = {
        auth_token: idToken,
        user_type: userType, // Ensure user type is selected
      };
  
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(bodyData),
      });
  
      const data = await response.json();
      console.log(data)
  
      if (data.status === 'Success') {
        setModalMessage('Registration successful via Google!');
        setModalVisible(true);
        navigate('/'); // Navigate to the home or login page
      } else {
        console.error('Backend registration error:', data);
        setModalMessage(data.status || 'Registration failed. Please try again.');
        setModalVisible(true);
      }
    } catch (error) {
      console.error('Error during Google registration:', error);
      setModalMessage('An unexpected error occurred during Google registration. Please try again.');
      setModalVisible(true);
    }
  };
  

  const handleRegister = async () => {
    if (userType === 'default') {
      setModalMessage('Please select a user type.');
      setModalVisible(true);
      return;
    }

    if (password !== confirmPassword) {
      setModalMessage('Passwords do not match.');
      setModalVisible(true);
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: username.trim(),
          email: email.trim(),
          password: password.trim(),
          user_type: userType,
        }),
      });

      const data = await response.json();

      if (data.status === 'Success') {
        setModalMessage('Registration successful!');
        setModalVisible(true);
        navigate('/'); // Redirect to login or home page
      } else {
        throw new Error(data.message || 'Registration failed. Please try again.');
      }
    } catch (error) {
      console.error('Error during Registration:', error);
      setModalMessage('An unexpected error occurred. Please try again.');
      setModalVisible(true);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Register</Text>
      <TextInput
        style={styles.input}
        placeholder="Username"
        placeholderTextColor="#aaa"
        value={username}
        onChangeText={setUsername}
      />
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
      <TextInput
        style={styles.input}
        placeholder="Confirm Password"
        placeholderTextColor="#aaa"
        value={confirmPassword}
        onChangeText={setConfirmPassword}
        secureTextEntry
      />

      <View style={styles.dropdownContainer}>
        <Text style={styles.label}>Select User Type:</Text>
        <select
          value={userType}
          onChange={(e) => setUserType(e.target.value)}
          style={styles.dropdown}
        >
          <option value="default">Select user type</option>
          <option value="fa">Fund Administrator</option>
          <option value="fm">Fund Manager</option>
        </select>
      </View>

      <TouchableOpacity style={styles.button} onPress={handleRegister}>
        <Text style={styles.buttonText}>Register</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.googleButton} onPress={handleGoogleRegister}>
        <Text style={styles.buttonText}>Register with Google</Text>
      </TouchableOpacity>

      <TouchableOpacity onPress={() => navigate('/login')} style={styles.loginLink}>
        <Text style={styles.loginText}>Already have an account? Login here</Text>
      </TouchableOpacity>

      <Modal
        animationType="slide"
        transparent={true}
        visible={modalVisible}
        onRequestClose={() => setModalVisible(false)}
      >
        <View style={styles.modalBackground}>
          <View style={styles.modalView}>
            <Text style={styles.modalText}>{modalMessage}</Text>
            <Pressable
              style={styles.closeButton}
              onPress={() => setModalVisible(false)}
            >
              <Text style={styles.closeButtonText}>OK</Text>
            </Pressable>
          </View>
        </View>
      </Modal>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
    width: "100%",
    backgroundColor: '#f0f0f0',
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  input: {
    width: '40%',
    height: 50,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 10,
    marginBottom: 15,
  },
  dropdownContainer: {
    width: '40%',
    marginBottom: 20,
  },
  label: {
    fontSize: 16,
    marginBottom: 5,
  },
  dropdown: {
    width: '100%',
    height: 40,
    padding: 10,
    borderRadius: 8,
    borderColor: '#ccc',
    borderWidth: 1,
    backgroundColor: '#F2F1F1',
  },
  button: {
    width: '40%',
    height: 50,
    backgroundColor: '#4CAF50',
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    marginBottom: 15,
  },
  googleButton: {
    width: '40%',
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
  loginLink: {
    marginTop: 20,
  },
  loginText: {
    color: '#4CAF50',
    fontSize: 16,
  },
  modalBackground: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  modalView: {
    width: '60%',
    backgroundColor: 'white',
    padding: 20,
    borderRadius: 10,
    alignItems: 'center',
  },
  modalText: {
    fontSize: 18,
    textAlign: 'center',
    marginBottom: 15,
  },
  closeButton: {
    backgroundColor: '#4CAF50',
    padding: 10,
    borderRadius: 8,
  },
  closeButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
});

export default Register;
