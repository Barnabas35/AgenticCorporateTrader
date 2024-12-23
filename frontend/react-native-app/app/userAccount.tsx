import React, { useEffect, useState } from 'react';
import { 
  View, 
  Text, 
  Image, 
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
  const [priceAlerts, setPriceAlerts] = useState<any[]>([]); 
  const [priceAlertModalVisible, setPriceAlertModalVisible] = useState(false);

  // Subscription state
  const [subscriptionActive, setSubscriptionActive] = useState<boolean | null>(null);
  const [subscriptionStart, setSubscriptionStart] = useState<number | null>(null);
  const [subscriptionEnd, setSubscriptionEnd] = useState<number | null>(null);
  const [renewSubscription, setRenewSubscription] = useState<boolean | null>(null);

  const navigate = useNavigate();

  if (!sessionToken) {
    navigate('/login');
    return null;
  }

  useEffect(() => {
    if (username && email && profileIconUrl && userType) {
      // Once user data and only when is loaded, fetch subscription
      fetchSubscriptionDetails();
    }
  }, [username, email, profileIconUrl, userType]);

  useEffect(() => {
    if (!sessionToken) {
      navigate('/login');
      return;
    }

    const fetchUserData = async () => {
      try {
        const [iconResponse, usernameResponse, emailResponse, userTypeResponse] = await Promise.all([
          fetch('https://tradeagently.dev/get-profile-icon', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ session_token: sessionToken }),
          }),
          fetch('https://tradeagently.dev/get-username', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ session_token: sessionToken }),
          }),
          fetch('https://tradeagently.dev/get-email', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ session_token: sessionToken }),
          }),
          fetch('https://tradeagently.dev/get-user-type', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ session_token: sessionToken }),
          })
        ]);

        const [iconData, usernameData, emailData, userTypeData] = await Promise.all([
          iconResponse.json(),
          usernameResponse.json(),
          emailResponse.json(),
          userTypeResponse.json()
        ]);

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

    if (!username || !email || !profileIconUrl || !userType) {
      fetchUserData();
    } else {
      setLoading(false);
    }
  }, [sessionToken, username, email, profileIconUrl, navigate, setUsername, setEmail, setProfileIconUrl, userType]);

  const fetchSubscriptionDetails = async () => {
    try {
      const response = await fetch('https://tradeagently.dev/get-subscription', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const data = await response.json();
      if (data.status === 'success') {
        setSubscriptionActive(data.subscription_active);
        setSubscriptionStart(data.subscription_start);
        setSubscriptionEnd(data.subscription_end);
        setRenewSubscription(data.renew_subscription);
      } else {
        setSubscriptionActive(false);
      }
    } catch (err) {
      setSubscriptionActive(false);
    }
  };

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
      console.log(result)
      if (result.status == 'Success') {
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
      } else if (data.status === 'No alerts found.') {
        setPriceAlerts([]);
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

  const activateSubscription = async () => {
    try {
      const response = await fetch('https://tradeagently.dev/activate-subscription', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const data = await response.json();
      if (data.status === 'success') {
        await fetchSubscriptionDetails();
      } else {
        setDebugMessage('Failed to activate subscription');
      }
    } catch (err) {
      setDebugMessage(`Error activating subscription: ${err}`);
    }
  };

  const cancelSubscription = async () => {
    try {
      const response = await fetch('https://tradeagently.dev/cancel-subscription', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const data = await response.json();
      if (data.status === 'success') {
        await fetchSubscriptionDetails();
      } else {
        setDebugMessage('Failed to cancel subscription');
      }
    } catch (err) {
      setDebugMessage(`Error cancelling subscription: ${err}`);
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

  const renderSubscriptionDetails = () => {
    if (subscriptionActive === null) {
      return <Text>Loading subscription details...</Text>;
    }

    if (!subscriptionActive) {
      // No active subscription
      return (
        <View style={{ alignItems: 'center' }}>
          <Text style={styles.subscriptionText}>AI subscription: Not Active</Text>
          <TouchableOpacity 
            style={[styles.actionButton, { backgroundColor: 'green' }]}
            onPress={activateSubscription}
          >
            <Text style={styles.actionButtonText}>Activate Subscription</Text>
          </TouchableOpacity>
        </View>
      );
    } else {
      // Subscription is active
      const startDate = subscriptionStart ? new Date(subscriptionStart * 1000).toLocaleString() : 'N/A';
      const endDate = subscriptionEnd ? new Date(subscriptionEnd * 1000).toLocaleString() : 'N/A';

      return (
        <View style={{ alignItems: 'center' }}>
          <Text style={styles.subscriptionText}>AI subscription: Active</Text>
          <Text style={styles.subscriptionDetails}>Start: {startDate}</Text>
          <Text style={styles.subscriptionDetails}>End: {endDate}</Text>
          <Text style={styles.subscriptionDetails}>
            Auto-Renew: {renewSubscription ? 'Yes' : 'No'}
          </Text>
          
          {renewSubscription ? (
            <TouchableOpacity 
              style={[styles.actionButton, { backgroundColor: '#FF494B', marginTop: 10 }]}
              onPress={cancelSubscription}
            >
              <Text style={styles.actionButtonText}>Cancel Subscription Renewal</Text>
            </TouchableOpacity>
          ) : (
            <TouchableOpacity 
              style={[styles.actionButton, { backgroundColor: 'green', marginTop: 10 }]}
              onPress={activateSubscription}
            >
              <Text style={styles.actionButtonText}>Reactivate Subscription Renewal</Text>
            </TouchableOpacity>
          )}
        </View>
      );
    }
  };

  return (
    <View style={styles.container}>
      {/* User Details Container including all buttons */}
      <View style={styles.detailsBox}>
        {profileIconUrl ? (
          <Image
            source={{ uri: profileIconUrl }}
            style={styles.profileIcon}
          />
        ) : (
          <Text style={styles.noProfileIconText}>No Profile Icon Available</Text>
        )}
        <View style={styles.userInfo}>
          <Text style={styles.infoLabel}>Username:</Text>
          <Text style={styles.infoValue}>{username}</Text>
        </View>
        <View style={styles.userInfo}>
          <Text style={styles.infoLabel}>Email:</Text>
          <Text style={styles.infoValue}>{email}</Text>
        </View>
        <View style={styles.userInfo}>
          <Text style={styles.infoLabel}>User Type:</Text>
          <Text style={styles.infoValue}>
            {userType === 'fm' ? 'Fund Manager' : userType === 'fa' ? 'Fund Admin' : userType}
          </Text>
        </View>

        {/* Subscription Details */}
        <View style={{ marginVertical: 20, width: '100%', alignItems: 'center' }}>
          {renderSubscriptionDetails()}
        </View>

        <TouchableOpacity style={styles.actionButton} onPress={handleLogout}>
          <Text style={styles.actionButtonText}>Logout</Text>
        </TouchableOpacity>

        <TouchableOpacity style={[styles.actionButton, styles.alertsButton]} onPress={fetchPriceAlerts}>
          <Text style={styles.actionButtonText}>View Price Alerts</Text>
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
            {priceAlerts.length === 0 ? (
              <Text style={styles.noAlertsText}>You have no price alerts.</Text>
            ) : (
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
            )}
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
  container: {
    flex: 1,
    justifyContent: 'flex-start',
    alignItems: 'center',
    paddingTop: 50,
    backgroundColor: '#f0f0f0',
    width: '100%',
  },
  detailsBox: {
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 20,
    alignItems: 'center',
    width: '30%',
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
  },
  profileIcon: {
    width: 100,
    height: 100,
    borderRadius: 50,
    marginBottom: 20,
  },
  noProfileIconText: {
    fontSize: 16,
    color: '#333',
    marginBottom: 20,
  },
  userInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 5,
  },
  infoLabel: {
    fontSize: 16,
    fontWeight: 'bold',
    marginRight: 5,
    color: '#555',
  },
  infoValue: {
    fontSize: 16,
    color: '#333',
  },
  subscriptionText: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 5,
    color: '#333',
  },
  subscriptionDetails: {
    fontSize: 14,
    color: '#333',
    marginVertical: 2,
  },
  actionButton: {
    backgroundColor: '#007bff', 
    padding: 10,
    borderRadius: 5,
    marginVertical: 10,
    width: '80%', 
    alignItems: 'center',
    justifyContent: 'center',
  },
  alertsButton: {
    backgroundColor: '#FFA500',
  },
  actionButtonText: {
    color: '#fff',
    fontWeight: 'bold',
    fontSize: 16,
    textAlign: 'center',
  },
  debugText: {
    fontSize: 12,
    color: 'red',
    marginTop: 10,
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
    maxHeight: '70%'
  },
  modalText: {
    fontSize: 18,
    textAlign: 'center',
    marginBottom: 15,
    color: '#333',
  },
  noAlertsText: {
    fontSize: 16,
    color: '#333',
    marginBottom: 15,
    textAlign: 'center',
  },
  button: {
    width: '80%',
    padding: 10,
    marginVertical: 5,
    borderRadius: 8,
  },
  buttonCancel: {
    backgroundColor: '#808080',
  },
  buttonDelete: {
    backgroundColor: '#FF3B30',
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
    textAlign: 'center',
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
});

export default UserAccount;
