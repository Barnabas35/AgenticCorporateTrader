import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Modal,
  Pressable,
  Image,
} from 'react-native';
import { useNavigate } from 'react-router-dom';
import { getAuth, GoogleAuthProvider, signInWithPopup } from 'firebase/auth';
import firebaseApp from '../components/firebase';

// Basic email regex
function validateEmail(email: string): boolean {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email.toLowerCase());
}

const googleIcon = require('../assets/images/google-icon.png');

const Register: React.FC = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [userType, setUserType] = useState('default'); // 'default' means "Select user type"

  // Error states for inline messages
  const [emailError, setEmailError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [userTypeError, setUserTypeError] = useState('');

  // Modal states
  const [modalVisible, setModalVisible] = useState(false);
  const [modalMessage, setModalMessage] = useState('');

  const navigate = useNavigate();
  const auth = getAuth(firebaseApp);
  const googleProvider = new GoogleAuthProvider();

  // ----------------------------------------------------
  // Google Registration
  // ----------------------------------------------------
  const handleGoogleRegister = async () => {
    // Clear previous errors first
    setUserTypeError('');

    // Make sure user type is selected
    if (userType === 'default') {
      setUserTypeError('Please select a user type before Google registration.');
      return;
    }

    try {
      const result = await signInWithPopup(auth, googleProvider);
      const user = result.user;

      if (!user) {
        throw new Error('User not found after Google sign-in.');
      }

      // Retrieve the ID token
      const idToken = await user.getIdToken(true);
      console.log('Google User ID Token:', idToken);
      console.log('Selected userType:', userType);

      // Send ID token & userType to backend
      const url = 'https://tradeagently.dev/register-with-token';
      const bodyData = {
        auth_token: idToken,
        user_type: userType,
      };

      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(bodyData),
      });

      const data = await response.json();
      console.log(data);

      if (data.status === 'Success') {
        setModalMessage('Registration successful via Google!');
        setModalVisible(true);
        navigate('/'); // redirect on success
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

  // ----------------------------------------------------
  // Direct (username/password) Registration
  // ----------------------------------------------------
  const handleRegister = async () => {
    // Clear old errors
    setEmailError('');
    setPasswordError('');
    setUserTypeError('');

    // 1) Validate user type
    if (userType === 'default') {
      setUserTypeError('Please select a user type.');
      return;
    }

    // 2) Validate email format
    if (!validateEmail(email)) {
      setEmailError('Please enter a valid email address.');
      return;
    }

    // 3) Validate password match
    if (password !== confirmPassword) {
      setPasswordError('Passwords do not match.');
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
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
        navigate('/'); // redirect after success
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

      {/* Username Input */}
      <TextInput
        style={styles.input}
        placeholder="Username"
        placeholderTextColor="#aaa"
        value={username}
        onChangeText={setUsername}
      />

      {/* Email + inline error */}
      {!!emailError && (
        <View style={styles.infoBox}>
          <Text style={styles.infoBoxText}>{emailError}</Text>
        </View>
      )}
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

      {/* Password + confirm + inline error */}
      {!!passwordError && (
        <View style={styles.infoBox}>
          <Text style={styles.infoBoxText}>{passwordError}</Text>
        </View>
      )}
      <TextInput
        style={styles.input}
        placeholder="Password"
        placeholderTextColor="#aaa"
        value={password}
        onChangeText={(val) => {
          setPassword(val);
          if (passwordError) setPasswordError('');
        }}
        secureTextEntry
      />
      <TextInput
        style={styles.input}
        placeholder="Confirm Password"
        placeholderTextColor="#aaa"
        value={confirmPassword}
        onChangeText={(val) => {
          setConfirmPassword(val);
          if (passwordError) setPasswordError('');
        }}
        secureTextEntry
      />

      {/* User Type + inline error */}
      {!!userTypeError && (
        <View style={styles.infoBox}>
          <Text style={styles.infoBoxText}>{userTypeError}</Text>
        </View>
      )}
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

      {/* Register Button */}
      <TouchableOpacity style={styles.button} onPress={handleRegister}>
        <Text style={styles.buttonText}>Register</Text>
      </TouchableOpacity>

      {/* Google login  Button w/ icon */}
      <TouchableOpacity style={styles.googleButton} onPress={handleGoogleRegister}>
        <View style={styles.googleButtonContent}>
          <Image source={googleIcon} style={styles.googleIcon} />
          <Text style={styles.buttonText}>Register with Google</Text>
        </View>
      </TouchableOpacity>

      {/* Link to login */}
      <TouchableOpacity onPress={() => navigate('/login')} style={styles.loginLink}>
        <Text style={styles.loginText}>Already have an account? Login here</Text>
      </TouchableOpacity>

      {/* Modal for success/failure messages */}
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

export default Register;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
    width: '100%',
    backgroundColor: '#f0f0f0',
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    marginBottom: 20,
  },
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
  dropdownContainer: {
    width: '30%',
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
  loginLink: {
    marginTop: 20,
  },
  loginText: {
    color: '#4CAF50',
    fontSize: 16,
  },
  modalBackground: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
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
