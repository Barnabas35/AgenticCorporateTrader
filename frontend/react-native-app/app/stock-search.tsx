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
  TextStyle,
  ViewStyle
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

interface Stock {
  symbol: string;
  company_name: string;
  price: number;
  currency: string;
}

interface StockDetails extends Stock {
  change_percentage: number;
  close_price: number;
  company_description: string;
  employee_count: number;
  high_price: number;
  homepage: string;
  low_price: number;
  open_price: number;
  volume: number;
}

interface Client {
  client_id: string;
  client_name: string;
}

const StockSearch: React.FC = () => {
  const [topStocks, setTopStocks] = useState<Stock[]>([]);
  const [searchResults, setSearchResults] = useState<Stock[]>([]);
  const [selectedStock, setSelectedStock] = useState<StockDetails | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sessionToken] = useSessionToken();
  const [historicalData, setHistoricalData] = useState<{ labels: string[]; prices: number[] }>({
    labels: [],
    prices: [],
  });
  const [historyWindow, setHistoryWindow] = useState<string>('month');
  const [interval, setIntervalValue] = useState<string>('day');

  const [buyModalVisible, setBuyModalVisible] = useState(false);
  const [buyAmount, setBuyAmount] = useState<string>('');
  const [isFundManager, setIsFundManager] = useState(false);
  const [clients, setClients] = useState<Client[]>([]);
  const [selectedClient, setSelectedClient] = useState<string | null>(null);
  const [priceAlertModalVisible, setPriceAlertModalVisible] = useState(false);
  const [alertPrice, setAlertPrice] = useState<string>(''); 
  const [alertTicker, setAlertTicker] = useState<string | null>(null);

  const [balance, setBalance] = useState<number | null>(null);

  useEffect(() => {
    fetchTopStocks();
    fetchUserType();
    fetchBalance();
  }, []);

  useEffect(() => {
    if (selectedStock) {
      fetchStockAggregates(selectedStock.symbol);
    }
  }, [interval, historyWindow, selectedStock]);

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

  const fetchTopStocks = async (limit = 10) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`https://tradeagently.dev/get-top-stocks?limit=${limit}`);
      const data = await response.json();
      if (data.status === 'Success') {
        setTopStocks(data.ticker_details);
      } else {
        setError('Failed to fetch top stocks');
      }
    } catch {
      setError('Failed to fetch top stocks');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (query: string) => {
    setSearchQuery(query);
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await fetch('https://tradeagently.dev/text-search-stock', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          search_query: query,
          limit: 5,
          session_token: sessionToken || '',
          show_price: true,
        }),
      });

      const data = await response.json();
      if (data.status === 'Success') {
        setSearchResults(data.ticker_details);
      } else {
        setError(data.message || 'No matching stocks found');
      }
    } catch (error) {
      console.error('Error searching stocks:', error);
      setError('Error searching stocks');
    } finally {
      setLoading(false);
    }
  };

  const fetchStockDetails = async (ticker: string, callback: () => void = () => {}) => {
    setLoading(true);
    try {
      const response = await fetch('https://tradeagently.dev/get-ticker-info', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ticker, session_token: sessionToken }),
      });
      const data = await response.json();

      if (data.status === 'Success') {
        setSelectedStock(data.ticker_info);
        callback();
      } else {
        Alert.alert('Error', 'Failed to fetch stock details.');
      }
    } catch (err) {
      console.error('Error fetching stock details:', err);
      Alert.alert('Error', 'An error occurred while fetching stock details.');
    } finally {
      setLoading(false);
    }
  };

  function getIntervalAndLimit(historyWindow: string): { interval: string; limit: number } {
    switch (historyWindow) {
      case 'hour':
        // Last hour: maybe 60 data points (1 per minute)
        return { interval: 'hour', limit: 60 };
      case 'day':
        // Last day: 24 data points (1 per hour)
        return { interval: 'hour', limit: 24 };
      case 'week':
        // Last week: 7 data points (1 per day)
        return { interval: 'day', limit: 7 };
      case 'month':
        // Last month: 30 data points (1 per day)
        return { interval: 'day', limit: 30 };
      case 'year':
        // Last year: 365 data points (1 per day)
        return { interval: 'day', limit: 365 };
      default:
        // Default: treat as month
        return { interval: 'day', limit: 30 };
    }
  }
  
  const fetchStockAggregates = async (ticker: string) => {
    setLoading(true);
    setError(null);
  
    const end = new Date();
    const endDate = end.toISOString().split('T')[0];
    let startDate = '';
  
    // Determine the start_date based on historyWindow without changing state
    switch (historyWindow) {
      case 'hour':
        const oneHourAgo = new Date(end.getTime() - 3600 * 1000);
        startDate = oneHourAgo.toISOString().split('T')[0];
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
        const defaultStart = new Date(end.getTime() - 30 * 86400 * 1000);
        startDate = defaultStart.toISOString().split('T')[0];
    }
  
    const { interval, limit } = getIntervalAndLimit(historyWindow);
  
    const requestBody = {
      ticker,
      session_token: sessionToken,
      start_date: startDate,
      end_date: endDate,
      interval,
      limit,
    };
  
    try {
      const response = await fetch('https://tradeagently.dev/get-ticker-aggregates', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
      });
      const data = await response.json();
  
      if (data.status === 'Success' && Array.isArray(data.ticker_info)) {
        let validData = data.ticker_info;
  
        if (historyWindow === 'hour' && validData.length === 0) {
          setHistoricalData({ labels: [], prices: [] });
          setError('No data available for the last hour timeframe.');
          setLoading(false);
          return;
        }
  
        // Reverse so oldest data is on the left
        validData.reverse();
  
        const labels = validData.map((item: any) => {
          const dateObj = new Date(item.timestamp);
          if (historyWindow === 'hour') {
            // hour timeframe: show hour:minute
            return dateObj.toLocaleString('en-US', {
              hour: 'numeric',
              minute: 'numeric',
              hour12: true,
            });
          } else if (historyWindow === 'day') {
            // day timeframe: show hour
            return dateObj.toLocaleString('en-US', {
              hour: 'numeric',
              hour12: true,
            });
          } else {
            // week/month/year: show date
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
        setError(data.message || 'Failed to fetch stock aggregates');
      }
    } catch (error) {
      console.error('Error fetching stock aggregates:', error);
      setError('Failed to fetch stock aggregates');
    } finally {
      setLoading(false);
    }
  };

  const handleBuyStock = async () => {
    if (!buyAmount || isNaN(parseFloat(buyAmount))) {
      Alert.alert('Error', 'Please enter a valid amount to buy.');
      return;
    }

    if ((isFundManager || clients.length > 1) && !selectedClient) {
      Alert.alert('Error', 'Please select a client.');
      return;
    }

    if (!selectedStock?.symbol) {
      Alert.alert('Error', 'No stock selected for purchase.');
      return;
    }

    const requestBody = {
      session_token: sessionToken,
      client_id: isFundManager
        ? selectedClient
        : clients.length === 1
        ? clients[0].client_id
        : undefined,
      ticker: selectedStock.symbol,
      usd_quantity: parseFloat(buyAmount),
      market: 'stocks',
    };

    try {
      const response = await fetch(`https://tradeagently.dev/purchase-asset`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
      });

      const data = await response.json();
      if (data.status === 'Success') {
        Alert.alert('Success', `Successfully purchased ${buyAmount} USD of ${selectedStock.symbol}.`);
        setBuyModalVisible(false);
        setBuyAmount('');
        setSelectedClient(null);
      } else {
        Alert.alert('Error', data.message || 'Failed to purchase stock.');
      }
    } catch (err) {
      console.error('Error buying stock:', err);
      Alert.alert('Error', 'An error occurred while buying the stock.');
    }
  };

  const confirmPriceAlert = async () => {
    if (!alertPrice || isNaN(parseFloat(alertPrice))) {
      Alert.alert('Error', 'Please enter a valid price.');
      return;
    }

    if (!alertTicker) {
      Alert.alert('Error', 'No stock selected for the price alert.');
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
          market: 'stocks',
        }),
      });

      const data = await response.json();
      if (data.status === 'Success') {
        Alert.alert(
          'Success',
          `Price alert set for ${alertTicker} at ${alertPrice} USD.`
        );
        setPriceAlertModalVisible(false);
        setAlertPrice('');
        setAlertTicker(null);
      } else {
        Alert.alert('Error', data.message || 'Failed to set the price alert.');
      }
    } catch (error) {
      console.error('Error setting price alert:', error);
      Alert.alert('Error', 'An error occurred while setting the price alert.');
    }
  };

  const renderStockItem = ({ item }: { item: Stock }) => (
    <View style={styles.cryptoItem}>
      <TouchableOpacity onPress={() => fetchStockDetails(item.symbol)}>
        <Text style={styles.cryptoSymbol}>{item.symbol}</Text>
        <Text>{item.company_name}</Text>
        <Text style={styles.priceText}>{item.price} {item.currency}</Text>
      </TouchableOpacity>
      <View style={styles.buttonContainer}>
        <TouchableOpacity style={styles.buyButton} onPress={() => fetchStockDetails(item.symbol, () => setBuyModalVisible(true))}>
          <Text style={styles.buyButtonText}>Buy</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.alertButton} onPress={() => {
          setAlertTicker(item.symbol);
          setPriceAlertModalVisible(true);
        }}>
          <Text style={styles.alertButtonText}>Set Price Alert</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  const changePercentageColor = selectedStock?.change_percentage && selectedStock.change_percentage < 0 ? 'red' : 'green';

  return (
    <ScrollView style={styles.container}>
      {!selectedStock && (
        <TextInput
          style={styles.searchInput}
          placeholder="Search for a stock..."
          value={searchQuery}
          onChangeText={(query) => {
            setSearchQuery(query);
            if (!query.trim()) {
              setSearchResults([]);
            } else {
              handleSearch(query);
            }
          }}
        />
      )}
      {error && <Text style={styles.errorText}>{error}</Text>}
      {loading && <ActivityIndicator size="large" color="#0000ff" />}
      {selectedStock ? (
        <View style={styles.cryptoDetails}>
          <Text style={styles.cryptoTitle}>
            {selectedStock.symbol} - {selectedStock.company_name}
          </Text>

          {/* Info Container */}
          <View style={styles.infoContainer}>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Change %:</Text>
              <Text style={[styles.infoValue, { color: changePercentageColor }]}>
                {(selectedStock.change_percentage * 100).toFixed(2)}%
              </Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Current Price:</Text>
              <Text style={styles.infoValue}>{selectedStock.close_price} {selectedStock.currency.toUpperCase()}</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Open:</Text>
              <Text style={styles.infoValue}>{selectedStock.open_price}</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>High:</Text>
              <Text style={[styles.infoValue, { color: 'green' }]}>{selectedStock.high_price}</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Low:</Text>
              <Text style={[styles.infoValue, { color: 'red' }]}>{selectedStock.low_price}</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Volume:</Text>
              <Text style={styles.infoValue}>{selectedStock.volume}</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Employees:</Text>
              <Text style={styles.infoValue}>{selectedStock.employee_count}</Text>
            </View>
            {selectedStock.homepage && (
              <View style={styles.infoRow}>
                <Text style={styles.infoLabel}>Homepage:</Text>
                <Text style={styles.infoValue}>{selectedStock.homepage}</Text>
              </View>
            )}
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
              <Line
                data={{
                  labels: historicalData.labels,
                  datasets: [
                    {
                      label: 'Price',
                      data: historicalData.prices,
                      borderColor: 'rgba(75,192,192,1)',
                      borderWidth: 1,
                      fill: false,
                    },
                  ],
                }}
                options={{
                  responsive: true,
                  plugins: { title: { display: true, text: 'Price History' } },
                  maintainAspectRatio: false,
                  scales: {
                    x: {
                      type: historyWindow === 'hour' ? 'time' : 'category',
                      time: { unit: historyWindow === 'hour' ? 'minute' : 'day' },
                    },
                  },
                }}
              />
            </View>
          </View>

          {/* Description Container */}
          <View style={styles.descriptionContainer}>
            <Text style={styles.description}>{selectedStock.company_description}</Text>
          </View>

          <TouchableOpacity
            style={[styles.backButton]}
            onPress={() => {
              setSelectedStock(null);
              setSearchResults([]);
            }}
          >
            <Text style={styles.backButtonText}>Back</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <FlatList
          data={searchQuery ? searchResults : topStocks}
          renderItem={renderStockItem}
          keyExtractor={(item) => item.symbol}
        />
      )}

      {/* Buy Modal */}
      <Modal visible={buyModalVisible} transparent={true} animationType="slide">
        <View style={styles.modalBackground}>
          <View style={styles.modalContainerSmall}>
            <Text style={styles.modalTitle}>Buy {selectedStock?.symbol}</Text>
            {balance !== null && (
              <Text style={styles.modalText}>Your Balance: ${balance.toFixed(2)}</Text>
            )}
            <Text style={styles.modalText}>Enter amount in USD:</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="USD Amount"
              keyboardType="numeric"
              value={buyAmount}
              onChangeText={setBuyAmount}
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
              <TouchableOpacity style={[styles.modalButton, styles.flexButton]} onPress={handleBuyStock}>
                <Text style={styles.modalButtonText}>Confirm Purchase</Text>
              </TouchableOpacity>
              <TouchableOpacity style={[styles.modalButton, styles.modalCancelButton, styles.flexButton]} onPress={() => setBuyModalVisible(false)}>
                <Text style={styles.modalButtonText}>Cancel</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      {/* Price Alert Modal */}
      <Modal visible={priceAlertModalVisible} transparent={true} animationType="slide">
        <View style={styles.modalBackground}>
          <View style={styles.modalContainerSmall}>
            <Text style={styles.modalTitle}>Set Price Alert</Text>
            {selectedStock && selectedStock.symbol === alertTicker && (
              <Text style={styles.modalText}>
                Current Price of {alertTicker}: ${selectedStock.close_price.toFixed(2)}
              </Text>
            )}
            <Text style={styles.modalText}>
              Enter the alert price for {alertTicker}:
            </Text>
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
  errorText: { color: 'red', marginBottom: 16 },
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
    alignSelf: 'flex-start', 
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
  chartContainer: { height: 300, width: '100%', marginVertical: 20 },

  descriptionContainer: {
    backgroundColor: '#f9f9f9',
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#ddd',
    padding: 16,
    marginBottom: 20,
  },
  description: { fontStyle: 'italic', fontSize: 16, textAlign: 'center', color: '#333' },

  backButton: { backgroundColor: '#007bff', padding: 8, borderRadius: 5, alignSelf: 'center', marginVertical: 10, width: '40%' },
  backButtonText: { color: 'white', fontWeight: 'bold', alignSelf: 'center' },
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

  modalBackground: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: 'rgba(0, 0, 0, 0.5)' },
  modalContainerSmall: { backgroundColor: 'white', padding: 20, borderRadius: 10, width: '70%' },
  modalTitle: { fontSize: 18, fontWeight: 'bold', marginBottom: 10, textAlign: 'center' },
  modalText: { fontSize: 16, marginBottom: 10, textAlign: 'center', color: '#333' },
  modalInput: { height: 40, borderColor: '#ccc', borderWidth: 1, borderRadius: 5, paddingHorizontal: 10, marginBottom: 10 },
  modalButtonContainer: { flexDirection: 'row', justifyContent: 'space-around', width: '100%', marginTop: 10 },
  modalButton: { backgroundColor: '#007bff', padding: 12, borderRadius: 5, alignItems: 'center', flex: 1, marginHorizontal: 5 },
  modalCancelButton: { backgroundColor: '#FF494B' },
  modalButtonText: { color: 'white', fontWeight: 'bold', textAlign: 'center' },
  flexButton: { flex: 1, marginHorizontal: 5 },

  dropdownContainer: { marginBottom: 16 },
  modalLabel: { fontSize: 16, fontWeight: 'bold', marginBottom: 8, color: '#333' },
});

export default StockSearch;
