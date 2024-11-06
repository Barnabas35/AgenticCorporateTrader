import React, { useEffect, useState } from 'react';
import { View, Text, Image, Button, StyleSheet, Modal, Pressable } from 'react-native';
import { useNavigate } from 'react-router-dom';
import { 
  useUsername, 
  useEmail, 
  useProfileIconUrl, 
  useSessionToken 
} from '../components/userContext';

const UserAccount: React.FC = () => {
  const [sessionToken, setSessionToken] = useSessionToken();
  const [username, setUsername] = useUsername();
  const [email, setEmail] = useEmail();
  const [profileIconUrl, setProfileIconUrl] = useProfileIconUrl();
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [debugMessage, setDebugMessage] = useState(''); // Debug message state
  const navigate = useNavigate();

  useEffect(() => {
    if (username && email && profileIconUrl) {
      setLoading(false);
      return;
    }

    if (!sessionToken) {
      navigate('/login-register');
      return;
    }

    const fetchUserData = async () => {
      try {
        const iconResponse = await fetch('https://tradeagently.dev/get-profile-icon', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ session_token: sessionToken }),
        });
        const iconData = await iconResponse.json();

        const usernameResponse = await fetch('https://tradeagently.dev/get-username', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ session_token: sessionToken }),
        });
        const usernameData = await usernameResponse.json();


        const emailResponse = await fetch('https://tradeagently.dev/get-email', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ session_token: sessionToken }),
        });
        const emailData = await emailResponse.json();

        if (usernameData.status === 'Success' && emailData.status === 'Success' && iconData.status === 'Success') {
          setUsername(usernameData.username);
          setEmail(emailData.email);
          setProfileIconUrl(iconData.url);
        } else {
          setModalVisible(true);
        }
      } catch (error) {
        setDebugMessage(`Error: ${error}`);
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [sessionToken, username, email, profileIconUrl, navigate, setUsername, setEmail, setProfileIconUrl]);

  const handleLogout = () => {
    setSessionToken(null);
    setEmail("");
    setUsername("");
    setProfileIconUrl("");
    navigate('/login-register');
  };

  const handleDeleteAccount = async () => {
    setModalVisible(true); // Show delete confirmation modal
  };

  const confirmDeleteAccount = async () => {
    try {
      const response = await fetch('https://tradeagently.dev/delete-user', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const result = await response.json();

      if (result.status === 'Success') {
        handleLogout(); // Log the user out and clear session
      } else {
        setDebugMessage('Failed to delete account');
      }
    } catch (error) {
      setDebugMessage(`Error during deletion: ${error}`);
    } finally {
      setModalVisible(false);
    }
  };

  if (loading) {
    return (
      <View style={styles.container}>
        <Text>Loading...</Text>
        {debugMessage ? <Text style={styles.debugText}>{debugMessage}</Text> : null}
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.detailsBox}>
        <View>
          {profileIconUrl ? (
            <Image
              source={{ uri: profileIconUrl }}
              style={styles.profileIcon}
            />
          ) : (
            <Text>No Profile Icon Available</Text>
          )}
        </View>
        <Text style={styles.text}>Username: {username}</Text>
        <Text style={styles.text}>Email: {email}</Text>
        <Button title="Logout" onPress={handleLogout} />
        <View style={styles.spacer} /> {/* Spacer between buttons */}
        <Button title="Delete Account" color="red" onPress={handleDeleteAccount} /> 
      </View>

      <Modal
        animationType="slide"
        transparent={true}
        visible={modalVisible}
        onRequestClose={() => setModalVisible(false)}
      >
        <View style={styles.modalBackground}>
          <View style={styles.modalView}>
            <Text style={styles.modalText}>
              Are you sure you want to delete your account? This action cannot be undone.
            </Text>
            <Pressable
              style={[styles.button, styles.buttonCancel]}
              onPress={() => setModalVisible(false)}
            >
              <Text style={styles.buttonText}>Cancel</Text>
            </Pressable>
            <Pressable
              style={[styles.button, styles.buttonDelete]}
              onPress={confirmDeleteAccount}
            >
              <Text style={styles.buttonText}>Delete</Text>
            </Pressable>
          </View>
        </View>
      </Modal>

      {debugMessage ? <Text style={styles.debugText}>{debugMessage}</Text> : null}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'flex-start',
    alignItems: 'center',
    paddingTop: 50,
    backgroundColor: '#f0f0f0',
    width: '100%',
  },
  profileIcon: {
    width: 100,
    height: 100,
    borderRadius: 50,
    marginBottom: 20,
  },
  text: {
    fontSize: 18,
    marginBottom: 10,
    color: '#333',
  },
  detailsBox: {
    backgroundColor: '#e0e0e0',
    borderRadius: 10,
    padding: 20,
    alignItems: 'center',
    width: '30%',
    marginBottom: 20,
  },
  spacer: {
    height: 20, // Space between buttons
  },
  modalBackground: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  modalView: {
    width: '40%',
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
  button: {
    width: '80%',
    padding: 10,
    marginVertical: 5,
    borderRadius: 8,
  },
  buttonCancel: {
    backgroundColor: '#ccc',
  },
  buttonDelete: {
    backgroundColor: '#FF3B30',
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
    textAlign: 'center',
  },
  debugText: {
    fontSize: 12,
    color: 'red',
    marginTop: 10,
  },
});

export default UserAccount;
