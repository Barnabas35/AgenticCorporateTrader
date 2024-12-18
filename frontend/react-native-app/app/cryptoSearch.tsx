import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  ScrollView,
  Modal,
  Alert,
} from 'react-native';
import { useSessionToken } from '../components/userContext';
import { Line } from 'react-chartjs-2';
import { Picker } from '@react-native-picker/picker';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
  TimeScale,
} from 'chart.js';
import 'chartjs-adapter-date-fns';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler, TimeScale);

interface Crypto {
  symbol: string;
  name: string;
  price: number;
}

interface CryptoDetails extends Crypto {
  high: number;
  low: number;
  volume: number;
  description: string;
  latest_price: number;
}

interface Client {
  client_id: string;
  client_name: string;
}

const CryptoSearch: React.FC = () => {
  const [topCryptos, setTopCryptos] = useState<Crypto[]>([]);
  const [searchResults, setSearchResults] = useState<Crypto[]>([]);
  const [selectedCrypto, setSelectedCrypto] = useState<CryptoDetails | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sessionToken] = useSessionToken();
  const [historicalData, setHistoricalData] = useState<{ labels: string[]; prices: number[] }>({
    labels: [],
    prices: [],
  });
  const [historyWindow, setHistoryWindow] = useState<string>('month');

  const [buyModalVisible, setBuyModalVisible] = useState(false);
  const [buyUsdQuantity, setBuyUsdQuantity] = useState<string>('');
  const [buyTicker, setBuyTicker] = useState<string>('');
  const [priceAlertModalVisible, setPriceAlertModalVisible] = useState(false);
  const [alertPrice, setAlertPrice] = useState<string>('');
  const [alertTicker, setAlertTicker] = useState<string | null>(null);
  const [isFundManager, setIsFundManager] = useState(false);
  const [clients, setClients] = useState<Client[]>([]);
  const [selectedClient, setSelectedClient] = useState<string | null>(null);
  const [balance, setBalance] = useState<number | null>(null);

  useEffect(() => {
    fetchTopCryptos();
    fetchUserType();
    fetchBalance();
  }, []);

  useEffect(() => {
    if (selectedCrypto) {
      fetchCryptoAggregates(selectedCrypto.symbol);
    }
  }, [historyWindow, selectedCrypto]);

  // Helper function to map historyWindow to interval and limit
  const getIntervalAndLimit = (hw: string): { interval: string; limit: number } => {
    switch (hw) {
      case 'hour':
        return { interval: '1m', limit: 60 }; // 1-minute intervals, 60 data points
      case 'day':
        return { interval: '1h', limit: 24 }; // 1-hour intervals, 24 data points
      case 'week':
        return { interval: '1d', limit: 7 }; // 1-day intervals, 7 data points
      case 'month':
        return { interval: '1d', limit: 30 }; // 1-day intervals, 30 data points
      case 'year':
        return { interval: '1mo', limit: 12 }; // 1-month intervals, 12 data points
      default:
        return { interval: '1d', limit: 30 }; // Default to month
    }
  };

  const fetchBalance = async () => {
    if (!sessionToken) return;
    try {
      const response = await fetch('https://tradeagently.dev/get-balance', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        setBalance(data.balance);
      }
    } catch (err) {
      console.error('Error fetching balance:', err);
    }
  };

  const fetchUserType = async () => {
    try {
      const response = await fetch(`https://tradeagently.dev/get-user-type`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        if (data.user_type === 'fm') {
          setIsFundManager(true);
          fetchClients();
        } else if (data.user_type === 'fa') {
          fetchClients();
        }
      }
    } catch (err) {
      console.error('Error fetching user type:', err);
    }
  };

  const fetchClients = async () => {
    try {
      const response = await fetch(`https://tradeagently.dev/get-client-list`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        setClients(data.clients);
        if (data.clients.length === 1) {
          setSelectedClient(data.clients[0].client_id);
        }
      } else {
        setError('Failed to fetch clients');
      }
    } catch (err) {
      console.error('Error fetching clients:', err);
    }
  };

  const fetchTopCryptos = async (limit = 10) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`https://tradeagently.dev/get-top-cryptos?limit=${limit}`);
      const data = await response.json();
      if (data.status === 'Success') {
        setTopCryptos(data.crypto_details);
      } else {
        setError('Failed to fetch top cryptos');
      }
    } catch {
      setError('Failed to fetch top cryptos');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (query: string) => {
    setSearchQuery(query);
    if (!query.trim()) return setSearchResults([]);

    setLoading(true);
    setError(null);
    try {
      const response = await fetch('https://tradeagently.dev/text-search-crypto', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          search_query: query,
          limit: 5,
          session_token: sessionToken,
          show_price: true,
        }),
      });

      const data = await response.json();
      if (data.status === 'Success' && data.crypto_details) {
        setSearchResults(data.crypto_details);
      } else {
        setError('No matching cryptocurrencies found');
      }
    } catch (error) {
      setError('Error searching cryptocurrencies');
    } finally {
      setLoading(false);
    }
  };

  const fetchCryptoDetails = async (symbol: string) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch('https://tradeagently.dev/get-crypto-info', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ crypto: symbol, session_token: sessionToken || '' }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        setSelectedCrypto(data.crypto_info);
        fetchCryptoAggregates(symbol);
      } else {
        setError(data.message || 'Failed to fetch crypto details');
      }
    } catch {
      setError('Failed to fetch crypto details');
    } finally {
      setLoading(false);
    }
  };

  const fetchCryptoAggregates = async (symbol: string) => {
    setLoading(true);
    setError(null);
    const end = new Date();
    const endDate = end.toISOString().split('T')[0];
    let startDate = '';

    // Determine start_date based on historyWindow
    switch (historyWindow) {
      case 'hour':
        // For the hour timeframe, set start_date to today
        startDate = endDate;
        break;
      case 'day':
        const oneDayAgo = new Date(end.getTime() - 86400 * 1000);
        startDate = oneDayAgo.toISOString().split('T')[0];
        break;
      case 'week':
        const oneWeekAgo = new Date(end.getTime() - 7 * 86400 * 1000);
        startDate = oneWeekAgo.toISOString().split('T')[0];
        break;
      case 'month':
        const oneMonthAgo = new Date(end.getTime() - 30 * 86400 * 1000);
        startDate = oneMonthAgo.toISOString().split('T')[0];
        break;
      case 'year':
        const oneYearAgo = new Date(end.getTime() - 365 * 86400 * 1000);
        startDate = oneYearAgo.toISOString().split('T')[0];
        break;
      default:
        startDate = '2024-01-01';
    }

    const { interval, limit } = getIntervalAndLimit(historyWindow);

    const requestBody = {
      crypto: symbol,
      session_token: sessionToken,
      start_date: startDate,
      end_date: endDate,
      interval: interval,
      limit: limit,
    };

    try {
      const response = await fetch('https://tradeagently.dev/get-crypto-aggregates', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        // Handle HTTP errors
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();

      if (data.status === 'Success' && Array.isArray(data.crypto_aggregates)) {
        let validData = data.crypto_aggregates;

        // Handle last hour timeframe with potential API issues
        if (historyWindow === 'hour' && validData.length === 0) {
          setHistoricalData({ labels: [], prices: [] });
          setError('No data available for the last hour timeframe.');
          setLoading(false);
          return;
        }

        // Reverse the data so the oldest is on the left
        validData.reverse();

        const labels = validData.map((item: any) => {
          const dateObj = new Date(item.date);
          if (historyWindow === 'hour') {
            // For last hour: show hour and minute
            return dateObj.toLocaleString('en-US', {
              hour: 'numeric',
              minute: 'numeric',
              hour12: true,
            });
          } else if (historyWindow === 'day') {
            // For last day: show hour only
            return dateObj.toLocaleString('en-US', {
              hour: 'numeric',
              hour12: true,
            });
          } else {
            // For week/month/year: show date
            return dateObj.toLocaleDateString('en-US', {
              year: 'numeric',
              month: 'short',
              day: 'numeric',
            });
          }
        });

        const prices = validData.map((item: any) => item.close);

        setHistoricalData({ labels, prices });
      } else {
        setError(data.message || 'Failed to fetch crypto aggregates');
      }
    } catch (error) {
      console.error('Error fetching crypto aggregates:', error);
      setError('Failed to fetch crypto aggregates');
    } finally {
      setLoading(false);
    }
  };

  const handleBuy = (ticker: string) => {
    setBuyTicker(ticker);
    setBuyModalVisible(true);
  };

  const handleConfirmBuy = async () => {
    if (!buyUsdQuantity || isNaN(parseFloat(buyUsdQuantity))) {
      Alert.alert('Error', 'Please enter a valid USD amount.');
      return;
    }

    if ((isFundManager || clients.length > 1) && !selectedClient) {
      Alert.alert('Error', 'Please select a client.');
      return;
    }

    if (!buyTicker) {
      Alert.alert('Error', 'Ticker is missing.');
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/purchase-asset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          usd_quantity: parseFloat(buyUsdQuantity),
          market: 'crypto',
          ticker: buyTicker,
          client_id: isFundManager ? selectedClient : undefined,
        }),
      });

      const data = await response.json();
      if (data.status === 'Success') {
        Alert.alert(
          'Success',
          `Successfully purchased ${buyUsdQuantity} USD of ${buyTicker}${
            isFundManager ? ` for client ${selectedClient}.` : '.'
          }`
        );
        setBuyModalVisible(false);
        setBuyUsdQuantity('');
        setSelectedClient(null);
      } else {
        Alert.alert('Error', data.message || 'Failed to complete the purchase.');
      }
    } catch (error) {
      Alert.alert('Error', 'An error occurred while processing the purchase.');
    }
  };

  const handleSetPriceAlert = (ticker: string) => {
    setAlertTicker(ticker);
    setPriceAlertModalVisible(true);
  };

  const confirmPriceAlert = async () => {
    if (!alertPrice || isNaN(parseFloat(alertPrice))) {
      Alert.alert('Error', 'Please enter a valid price.');
      return;
    }

    if (!alertTicker) {
      Alert.alert('Error', 'No crypto selected for the price alert.');
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/create-price-alert', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          ticker: alertTicker,
          price: parseFloat(alertPrice),
          market: 'crypto',
        }),
      });

      const data = await response.json();
      if (data.status === 'Success') {
        Alert.alert('Success', `Price alert set for ${alertTicker} at ${alertPrice} USD.`);
        setPriceAlertModalVisible(false);
        setAlertPrice('');
        setAlertTicker(null);
      } else {
        Alert.alert('Error', data.message || 'Failed to set the price alert.');
      }
    } catch (error) {
      Alert.alert('Error', 'An error occurred while setting the price alert.');
    }
  };

  const renderCryptoItem = ({ item }: { item: Crypto }) => (
    <View style={styles.cryptoItem}>
      <TouchableOpacity onPress={() => fetchCryptoDetails(item.symbol)}>
        <Text style={styles.cryptoSymbol}>{item.symbol}</Text>
        <Text>{item.name}</Text>
        <Text style={styles.priceText}>{item.price} USD</Text>
      </TouchableOpacity>
      <View style={styles.buttonContainer}>
        <TouchableOpacity style={styles.buyButton} onPress={() => handleBuy(item.symbol)}>
          <Text style={styles.buyButtonText}>Buy</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.alertButton} onPress={() => handleSetPriceAlert(item.symbol)}>
          <Text style={styles.alertButtonText}>Set Price Alert</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  return (
    <ScrollView style={styles.container}>
      {!selectedCrypto && (
        <TextInput
          style={styles.searchInput}
          placeholder="Search for a crypto..."
          value={searchQuery}
          onChangeText={handleSearch}
        />
      )}
      {error && <Text style={styles.errorText}>{error}</Text>}
      {loading && <ActivityIndicator size="large" color="#0000ff" />}
      {selectedCrypto ? (
        <View style={styles.cryptoDetails}>
          <Text style={styles.cryptoTitle}>
            {selectedCrypto.symbol} - {selectedCrypto.name}
          </Text>

          {/* Info Container */}
          <View style={styles.infoContainer}>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Latest Price:</Text>
              <Text style={styles.infoValue}>{selectedCrypto.latest_price} USD</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>High:</Text>
              <Text style={[styles.infoValue, { color: 'green' }]}>{selectedCrypto.high} USD</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Low:</Text>
              <Text style={[styles.infoValue, { color: 'red' }]}>{selectedCrypto.low} USD</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Volume:</Text>
              <Text style={styles.infoValue}>{selectedCrypto.volume}</Text>
            </View>
          </View>

          {/* Historical Data & Chart Container */}
          <View style={styles.chartSection}>
            <Text style={styles.sectionTitle}>Historical Data</Text>
            <View style={styles.dropdownWrapper}>
              <Text style={styles.dropdownLabel}>Select Timeframe:</Text>
              <View style={styles.customPickerContainer}>
                <Picker
                  selectedValue={historyWindow}
                  onValueChange={(value) => setHistoryWindow(value)}
                  style={styles.pickerStyle}
                >
                  <Picker.Item label="Last Hour" value="hour" />
                  <Picker.Item label="Last Day" value="day" />
                  <Picker.Item label="Last Week" value="week" />
                  <Picker.Item label="Last Month" value="month" />
                  <Picker.Item label="Last Year" value="year" />
                </Picker>
              </View>
            </View>

            <View style={styles.chartContainer}>
              {historicalData.labels.length > 0 ? (
                <Line
                  data={{
                    labels: historicalData.labels,
                    datasets: [
                      {
                        label: 'Price (USD)',
                        data: historicalData.prices,
                        borderColor: 'rgba(75,192,192,1)',
                        borderWidth: 2,
                        fill: false,
                      },
                    ],
                  }}
                  options={{
                    responsive: true,
                    plugins: {
                      title: { display: true, text: 'Price History' },
                      legend: { display: false },
                    },
                    maintainAspectRatio: false,
                    scales: {
                      x: {
                        type: historyWindow === 'hour' ? 'category' : 'category',
                        ticks: {
                          autoSkip: true,
                          maxTicksLimit: historyWindow === 'hour' ? 10 : 10,
                        },
                      },
                      y: {
                        beginAtZero: false,
                      },
                    },
                  }}
                />
              ) : (
                <Text style={styles.noDataText}>No data available for the selected timeframe.</Text>
              )}
            </View>
          </View>

          {/* Description Container */}
          <View style={styles.descriptionContainer}>
            <Text style={styles.description}>{selectedCrypto.description}</Text>
          </View>

          <TouchableOpacity
            style={styles.backButton}
            onPress={() => {
              setSelectedCrypto(null);
              setSearchResults([]);
              setHistoricalData({ labels: [], prices: [] });
              setError(null);
            }}
          >
            <Text style={styles.backButtonText}>Back</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <FlatList
          data={searchQuery ? searchResults : topCryptos}
          renderItem={renderCryptoItem}
          keyExtractor={(item) => item.symbol}
        />
      )}

      {/* Buy Modal */}
      <Modal visible={buyModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Buy {buyTicker}</Text>
            {balance !== null && (
              <Text style={styles.modalText}>Your Balance: ${balance.toFixed(2)}</Text>
            )}
            <Text style={styles.modalText}>Enter amount in USD:</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="USD Amount"
              keyboardType="numeric"
              value={buyUsdQuantity}
              onChangeText={setBuyUsdQuantity}
            />
            {isFundManager && (
              <View style={styles.dropdownContainer}>
                <Text style={styles.modalLabel}>Select Client:</Text>
                <View style={styles.customPickerContainer}>
                  <Picker
                    selectedValue={selectedClient}
                    onValueChange={(value) => setSelectedClient(value)}
                    style={styles.pickerStyle}
                  >
                    <Picker.Item label="Select a client" value={null} />
                    {clients.map((client) => (
                      <Picker.Item key={client.client_id} label={client.client_name} value={client.client_id} />
                    ))}
                  </Picker>
                </View>
              </View>
            )}
            <View style={styles.modalButtonContainer}>
              <TouchableOpacity style={[styles.modalButton, styles.flexButton]} onPress={handleConfirmBuy}>
                <Text style={styles.modalButtonText}>Confirm Purchase</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalButton, styles.modalCancelButton, styles.flexButton]}
                onPress={() => setBuyModalVisible(false)}
              >
                <Text style={styles.modalButtonText}>Cancel</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      {/* Price Alert Modal */}
      <Modal visible={priceAlertModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Set Price Alert</Text>
            {selectedCrypto && selectedCrypto.symbol === alertTicker && (
              <Text style={styles.modalText}>
                Current Price of {alertTicker}: ${selectedCrypto.latest_price?.toFixed(2)}
              </Text>
            )}
            <Text style={styles.modalText}>Enter the alert price for {alertTicker}:</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="Alert Price"
              keyboardType="numeric"
              value={alertPrice}
              onChangeText={setAlertPrice}
            />
            <View style={styles.modalButtonContainer}>
              <TouchableOpacity style={[styles.modalButton, styles.flexButton]} onPress={confirmPriceAlert}>
                <Text style={styles.modalButtonText}>Confirm Alert</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalButton, styles.modalCancelButton, styles.flexButton]}
                onPress={() => setPriceAlertModalVisible(false)}
              >
                <Text style={styles.modalButtonText}>Cancel</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, width: '100%' },
  searchInput: { height: 40, borderColor: 'gray', borderWidth: 1, marginBottom: 16, paddingHorizontal: 8 },
  errorText: { color: 'red', marginBottom: 16, textAlign: 'center' },
  sectionTitle: { fontSize: 20, fontWeight: 'bold', marginBottom: 8, textAlign: 'center' },
  cryptoItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#ddd',
  },
  cryptoSymbol: { fontWeight: 'bold', fontSize: 16 },
  priceText: { color: 'green', fontWeight: 'bold' },
  cryptoDetails: { padding: 16 },
  cryptoTitle: { fontSize: 24, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },

  infoContainer: {
    backgroundColor: '#f9f9f9',
    borderRadius: 10,
    padding: 16,
    marginBottom: 20,
    borderWidth: 1,
    borderColor: '#ddd',
    alignSelf: 'flex-start', // Align to the left
    width: '100%',
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    marginBottom: 8,
  },
  infoLabel: { fontWeight: 'bold', fontSize: 16, color: '#333', width: 120 },
  infoValue: { fontSize: 16, color: '#555' },

  chartSection: {
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 16,
    marginBottom: 20,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  dropdownWrapper: {
    marginBottom: 20,
  },
  dropdownLabel: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  customPickerContainer: {
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#ccc',
    backgroundColor: '#fff',
    overflow: 'hidden',
  },
  pickerStyle: {
    height: 40,
  },
  chartContainer: { height: 300, width: '100%', marginVertical: 20, justifyContent: 'center', alignItems: 'center' },
  noDataText: { textAlign: 'center', color: 'gray', marginTop: 20 },

  // Description container
  descriptionContainer: {
    backgroundColor: '#f9f9f9',
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#ddd',
    padding: 16,
    marginBottom: 20,
  },
  description: { fontStyle: 'italic', fontSize: 16, textAlign: 'center', color: '#333' },

  backButton: {
    backgroundColor: '#007bff',
    padding: 10,
    borderRadius: 5,
    alignSelf: 'center',
    marginVertical: 10,
    width: '40%',
  },
  backButtonText: { color: 'white', fontWeight: 'bold', textAlign: 'center' },

  buttonContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  buyButton: {
    backgroundColor: '#007bff',
    padding: 10,
    borderRadius: 5,
    marginRight: 8,
  },
  buyButtonText: { color: 'white', fontWeight: 'bold' },
  alertButton: { backgroundColor: '#FFAA00', padding: 10, borderRadius: 5 },
  alertButtonText: { color: 'white', fontWeight: 'bold' },

  // Modal Styles
  modalBackground: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  modalContainer: {
    backgroundColor: 'white',
    padding: 20,
    borderRadius: 10,
    width: '80%',
  },
  modalTitle: { fontSize: 18, fontWeight: 'bold', marginBottom: 10, textAlign: 'center' },
  modalText: { fontSize: 16, marginBottom: 10, textAlign: 'center', color: '#333' },
  modalInput: {
    height: 40,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 5,
    paddingHorizontal: 10,
    marginBottom: 10,
  },
  modalButtonContainer: { flexDirection: 'row', justifyContent: 'space-around', width: '100%', marginTop: 10 },
  modalButton: {
    backgroundColor: '#007bff',
    padding: 12,
    borderRadius: 5,
    alignItems: 'center',
    flex: 1,
    marginHorizontal: 5,
  },
  modalCancelButton: { backgroundColor: '#FF494B' },
  modalButtonText: { color: 'white', fontWeight: 'bold', textAlign: 'center' },
  flexButton: { flex: 1, marginHorizontal: 5 },

  dropdownContainer: { marginBottom: 16 },
  modalLabel: { fontSize: 16, fontWeight: 'bold', marginBottom: 8, color: '#333' },
});

export default CryptoSearch;
