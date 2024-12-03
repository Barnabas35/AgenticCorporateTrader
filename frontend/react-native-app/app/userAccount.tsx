import React, { useEffect, useState } from 'react';
import { 
  View, 
  Text, 
  Image, 
  Button, 
  StyleSheet, 
  Modal, 
  Pressable, 
  FlatList, 
  TouchableOpacity 
} from 'react-native';
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
  const [debugMessage, setDebugMessage] = useState('');
  const [userType, setUserType] = useState<string | null>(null);
  const [priceAlerts, setPriceAlerts] = useState<any[]>([]); // Store price alerts
  const [priceAlertModalVisible, setPriceAlertModalVisible] = useState(false); // Manage price alert modal
  const navigate = useNavigate();

  useEffect(() => {
    if (username && email && profileIconUrl && userType) {
      setLoading(false);
      return;
    }

    if (!sessionToken) {
      navigate('/login');
      return;
    }

    const fetchUserData = async () => {
      try {
        const iconResponse = await fetch('https://tradeagently.dev/get-profile-icon', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ session_token: sessionToken }),
        });
        const iconData = await iconResponse.json();

        const usernameResponse = await fetch('https://tradeagently.dev/get-username', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ session_token: sessionToken }),
        });
        const usernameData = await usernameResponse.json();

        const emailResponse = await fetch('https://tradeagently.dev/get-email', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ session_token: sessionToken }),
        });
        const emailData = await emailResponse.json();

        const userTypeResponse = await fetch('https://tradeagently.dev/get-user-type', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ session_token: sessionToken }),
        });
        const userTypeData = await userTypeResponse.json();

        if (
          usernameData.status === 'Success' &&
          emailData.status === 'Success' &&
          iconData.status === 'Success' &&
          userTypeData.status === 'Success'
        ) {
          setUsername(usernameData.username);
          setEmail(emailData.email);
          setProfileIconUrl(iconData.url);
          setUserType(userTypeData.user_type);
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
    setModalVisible(true);
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
        handleLogout();
      } else {
        setDebugMessage('Failed to delete account');
      }
    } catch (error) {
      setDebugMessage(`Error during deletion: ${error}`);
    } finally {
      setModalVisible(false);
    }
  };

  const fetchPriceAlerts = async () => {
    try {
      const response = await fetch('https://tradeagently.dev/get-price-alerts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const data = await response.json();
  
      if (data.status === 'Success') {
        setPriceAlerts(data.alerts || []);
        setPriceAlertModalVisible(true);
      } else {
        setDebugMessage('Failed to fetch price alerts');
      }
    } catch (error) {
      setDebugMessage(`Error fetching price alerts: ${error}`);
    }
  };
  

  const deletePriceAlert = async (alertId: string) => {
    try {
      const response = await fetch('https://tradeagently.dev/delete-price-alert', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken, alert_id: alertId }),
      });
      const data = await response.json();

      if (data.status === 'Success') {
        setPriceAlerts((prevAlerts) =>
          prevAlerts.filter((alert) => alert.alert_id !== alertId)
        );
      } else {
        setDebugMessage('Failed to delete price alert');
      }
    } catch (error) {
      setDebugMessage(`Error deleting price alert: ${error}`);
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
        <Text style={styles.text}>
          User Type: {userType === 'fm' ? 'Fund Manager' : userType === 'fa' ? 'Fund Admin' : userType}
        </Text>
        <TouchableOpacity style={styles.arrowButton} onPress={fetchPriceAlerts}>
          <Text style={styles.arrowButtonText}>View Price Alerts</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.actionButton} onPress={handleLogout}>
          <Text style={styles.actionButtonText}>Logout</Text>
        </TouchableOpacity>
        {userType !== 'admin' && (
          <TouchableOpacity
            style={[styles.actionButton, { backgroundColor: '#FF494B' }]}
            onPress={handleDeleteAccount}
          >
            <Text style={styles.actionButtonText}>Delete Account</Text>
          </TouchableOpacity>
        )}
      </View>

      {/* Modal for Price Alerts */}
      <Modal
        animationType="slide"
        transparent={true}
        visible={priceAlertModalVisible}
        onRequestClose={() => setPriceAlertModalVisible(false)}
      >
        <View style={styles.modalBackground}>
          <View style={styles.modalView}>
            <Text style={styles.modalText}>Your Price Alerts</Text>
            <FlatList
              data={priceAlerts}
              keyExtractor={(item) => item.alert_id}
              renderItem={({ item }) => (
                <View style={styles.alertCard}>
                  <View style={styles.alertContent}>
                    <Text style={styles.alertText}>
                      <Text style={styles.alertLabel}>Market:</Text> {item.market}
                    </Text>
                    <Text style={styles.alertText}>
                      <Text style={styles.alertLabel}>Ticker:</Text> {item.ticker}
                    </Text>
                    <Text style={styles.alertText}>
                      <Text style={styles.alertLabel}>Price:</Text> {item.price}
                    </Text>
                  </View>
                  <TouchableOpacity
                    style={styles.deleteButton}
                    onPress={() => deletePriceAlert(item.alert_id)}
                  >
                    <Text style={styles.deleteButtonText}>Delete</Text>
                  </TouchableOpacity>
                </View>
              )}
            />
            <Pressable
              style={[styles.button, styles.buttonCancel]}
              onPress={() => setPriceAlertModalVisible(false)}
            >
              <Text style={styles.buttonText}>Close</Text>
            </Pressable>
          </View>
        </View>
      </Modal>

      {/* Modal for Delete Account Confirmation */}
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
  // Existing styles
  container: {
    flex: 1,
    justifyContent: 'flex-start',
    alignItems: 'center',
    paddingTop: 50,
    backgroundColor: '#f0f0f0',
    width: '100%',
  },
  arrowButton: {
    backgroundColor: '#007bff',
    padding: 10,
    borderRadius: 5,
    marginVertical: 10,
  },
  arrowButtonText: {
    color: '#fff',
    fontWeight: 'bold',
    textAlign: 'center',
  },
  alertItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
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
    height: 20,
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
    backgroundColor: '#FF3F41',
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
  alertCard: {
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 16,
    marginVertical: 8,
    marginHorizontal: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
    elevation: 3,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  alertContent: {
    flex: 1,
  },
  alertText: {
    fontSize: 16,
    color: '#333',
    marginBottom: 4,
  },
  alertLabel: {
    fontWeight: 'bold',
    color: '#555',
  },
  deleteButton: {
    backgroundColor: '#FF494B',
    borderRadius: 8,
    paddingVertical: 8,
    paddingHorizontal: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginLeft: 16,
  },
  deleteButtonText: {
    color: '#fff',
    fontWeight: 'bold',
    fontSize: 14,
  },
  actionButton: {
    backgroundColor: '#007bff', // Default blue color for buttons
    padding: 10,
    borderRadius: 5,
    marginVertical: 10,
    width: '80%', // Make the button consistent in size
    alignItems: 'center',
    justifyContent: 'center',
  },
  actionButtonText: {
    color: '#fff',
    fontWeight: 'bold',
    fontSize: 16,
    textAlign: 'center',
  },
});

export default UserAccount;
