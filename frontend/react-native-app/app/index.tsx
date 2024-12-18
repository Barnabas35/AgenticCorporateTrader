import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom'; // Import for navigation
import {
  View,
  Text,
  StyleSheet,
  ActivityIndicator,
  TextInput,
  TouchableOpacity,
} from 'react-native';
import { useSessionToken, useUser } from '../components/userContext';
import BitcoinChart from '../components/BitcoinChart';
import { MaterialIcons } from '@expo/vector-icons';

const Home: React.FC = () => {
  const { username } = useUser();
  const [sessionToken] = useSessionToken();
  const [balance, setBalance] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [addAmount, setAddAmount] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>(''); // State for error messages
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
          setErrorMessage('Failed to fetch balance. Please try again.');
        }
      } catch (error) {
        console.error('Error fetching balance:', error);
        setErrorMessage('An error occurred while fetching balance.');
      } finally {
        setLoading(false);
      }
    };

    fetchBalance();
  }, [sessionToken]);

  const handleAddBalance = async () => {
    // Clear any previous error messages
    setErrorMessage('');

    // Trim whitespace from input
    const trimmedAmount = addAmount.trim();

    // Validation: Check if input is empty
    if (trimmedAmount === '') {
      setErrorMessage('Please input an amount.');
      return;
    }

    // Validation: Check if input is a valid positive number
    const numericAmount = Number(trimmedAmount);
    if (isNaN(numericAmount) || numericAmount <= 0) {
      setErrorMessage('Please input a valid positive number.');
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/add-balance', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          usd_quantity: numericAmount,
        }),
      });

      const data = await response.json();
      console.log(data);
      if (data.status === 'Success') {
        // Redirect to CheckoutForm with clientSecret and amount
        navigate('/checkout', {
          state: { 
            clientSecret: data.client_secret, 
            dpmCheckerLink: data.dpmCheckerLink,
            amount: numericAmount, // Pass the amount here
          },
        });
      } else {
        setErrorMessage(data.message || 'Failed to initiate payment. Please try again.');
      }
    } catch (error) {
      console.error('Error initiating payment:', error);
      setErrorMessage('An error occurred while initiating payment.');
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.textContainer}>
        <View style={styles.headerContainer}>
          <Text style={styles.title}>Welcome to TradeAgently, {username}!</Text>
        </View>
        <Text style={styles.description}>
          TradeAgently is your trusted partner in managing and tracking your investments with real-time data and insightful analytics.
        </Text>
        <View style={styles.featureContainer}>
          <MaterialIcons name="show-chart" size={28} color="#2e86de" />
          <Text style={styles.featureText}>Real-time tracking of investment performance to keep you ahead.</Text>
        </View>
        <View style={styles.featureContainer}>
          <MaterialIcons name="security" size={28} color="#2e86de" />
          <Text style={styles.featureText}>Secure and reliable platform ensuring your data safety.</Text>
        </View>
        {/* Conditionally render the "Get Started" message if not logged in */}
        {!sessionToken && (
          <Text style={styles.ctaText}>
            To get started, log in or register, and begin tracking your investments today!
          </Text>
        )}
        {/* Additional information about the website */}
        <Text style={styles.additionalInfo}>
          Whether you're a seasoned investor or just starting out, TradeAgently provides the tools you need to make informed decisions and grow your portfolio.
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
                  <TouchableOpacity style={styles.addBalanceButton} onPress={handleAddBalance}>
                    <Text style={styles.addBalanceButtonText}>Add Balance</Text>
                  </TouchableOpacity>
                </View>
                {/* Display Error Message */}
                {errorMessage !== '' && (
                  <Text style={styles.errorText}>{errorMessage}</Text>
                )}
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
    flexShrink: 1, // Allow text to wrap if needed
  },
  ctaText: {
    fontSize: 18,
    color: '#333',
    marginBottom: 15,
    lineHeight: 26,
    fontWeight: '500',
  },
  additionalInfo: {
    fontSize: 16,
    color: '#555',
    marginTop: 10,
    lineHeight: 24,
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
    marginBottom: 10,
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
    backgroundColor: '#fff',
    color: '#333',
  },
  addBalanceButton: {
    backgroundColor: '#27ae60', // Green color
    paddingVertical: 10,
    paddingHorizontal: 20,
    borderRadius: 5,
    width: '80%',
    alignItems: 'center',
  },
  addBalanceButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  errorText: {
    color: '#e74c3c',
    fontSize: 14,
    marginTop: 10,
    textAlign: 'center',
  },
});
