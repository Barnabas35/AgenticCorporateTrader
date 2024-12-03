import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom'; // Import for navigation
import { View, Text, StyleSheet, ActivityIndicator, TextInput, Button, Alert } from 'react-native';
import { useSessionToken, useUser } from '../components/userContext';
import BitcoinChart from '../components/BitcoinChart';
import { FontAwesome5, MaterialIcons } from '@expo/vector-icons';

const Home: React.FC = () => {
  const { username } = useUser();
  const [sessionToken] = useSessionToken();
  const [balance, setBalance] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [addAmount, setAddAmount] = useState<string>('');
  const navigate = useNavigate(); // React Router navigation

  useEffect(() => {
    const fetchBalance = async () => {
      if (!sessionToken) {
        setLoading(false);
        return;
      }

      try {
        const response = await fetch('https://tradeagently.dev/get-balance', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ session_token: sessionToken }),
        });
        const data = await response.json();

        if (data.status === 'Success') {
          setBalance(data.balance);
        } else {
          console.error('Failed to fetch balance:', data);
        }
      } catch (error) {
        console.error('Error fetching balance:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchBalance();
  }, [sessionToken]);

  const handleAddBalance = async () => {
    if (!addAmount || isNaN(Number(addAmount))) {
      Alert.alert('Invalid Amount', 'Please enter a valid number.');
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/add-balance', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          usd_quantity: Number(addAmount),
        }),
      });

      const data = await response.json();
      console.log(data)
      if (data.status === 'Success') {
        // Redirect to CheckoutForm with the clientSecret
        navigate('/checkout', {
          state: { clientSecret: data.client_secret, dpmCheckerLink: data.dpmCheckerLink },
        });
      } else {
        Alert.alert('Error', 'Failed to initiate payment. Please try again.');
      }
    } catch (error) {
      console.error('Error initiating payment:', error);
      Alert.alert('Error', 'An error occurred while initiating payment.');
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.textContainer}>
        <View style={styles.headerContainer}>
          <Text style={styles.title}>Welcome to TradeAgently, {username}!</Text>
        </View>
        <Text style={styles.description}>
          This platform is designed to help users efficiently manage and track their investments.
        </Text>
        <View style={styles.featureContainer}>
          <MaterialIcons name="show-chart" size={28} color="#2e86de" />
          <Text style={styles.featureText}>Real-time tracking of investment performance to keep you ahead.</Text>
        </View>
        <Text style={styles.description}>
          To get started, log in or register, and begin tracking your investments today!
        </Text>
      </View>
      <View style={styles.rightContainer}>
        {sessionToken && (
          <View style={styles.balanceContainer}>
            {loading ? (
              <ActivityIndicator size="large" color="#2e86de" />
            ) : (
              <>
                <Text style={styles.balanceText}>
                  Balance: ${balance?.toFixed(2) ?? '0.00'}
                </Text>
                <View style={styles.addBalanceContainer}>
                  <TextInput
                    style={styles.input}
                    placeholder="Amount to add"
                    keyboardType="numeric"
                    value={addAmount}
                    onChangeText={setAddAmount}
                  />
                  <Button title="Add Balance" onPress={handleAddBalance} />
                </View>
              </>
            )}
          </View>
        )}
        <BitcoinChart />
      </View>
    </View>
  );
};

export default Home;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    backgroundColor: '#f0f0f0',
    padding: 20,
    width: '100%',
  },
  textContainer: {
    flex: 1,
    paddingRight: 20,
    justifyContent: 'flex-start',
  },
  headerContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20,
  },
  title: {
    fontSize: 26,
    fontWeight: 'bold',
    color: '#2e86de',
  },
  description: {
    fontSize: 18,
    color: '#333',
    marginBottom: 15,
    lineHeight: 26,
  },
  featureContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 15,
  },
  featureText: {
    fontSize: 18,
    color: '#333',
    marginLeft: 10,
  },
  rightContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'flex-start',
  },
  balanceContainer: {
    width: '90%',
    padding: 15,
    borderRadius: 10,
    backgroundColor: '#2e86de',
    marginBottom: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  balanceText: {
    fontSize: 18,
    color: '#fff',
    fontWeight: 'bold',
  },
  addBalanceContainer: {
    marginTop: 10,
    width: '100%',
    alignItems: 'center',
  },
  input: {
    height: 40,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 5,
    paddingHorizontal: 10,
    marginBottom: 10,
    width: '80%',
  },
});
