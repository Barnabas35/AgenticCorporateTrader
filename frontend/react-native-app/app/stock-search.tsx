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



  useEffect(() => {
    fetchTopStocks();
    fetchUserType();
  }, []);

  useEffect(() => {
    if (selectedStock) {
      fetchStockAggregates(selectedStock.symbol);
    }
  }, [interval, historyWindow, selectedStock]);

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
    } catch (error) {
      console.error('Error fetching top stocks:', error);
      setError('Failed to fetch top stocks');
    } finally {
      setLoading(false);
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
          fetchClients(); // Fetch clients for Fund Managers
        } else if (data.user_type === 'fa') {
          fetchClients(); // Fetch client for Fund Administrator
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
        // Automatically set the selected client if the user is a Fund Administrator
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

  const fetchStockAggregates = async (ticker: string) => {
    setLoading(true);
    setError(null);
    let startDate = '';
    const endDate = new Date().toISOString().split('T')[0];
    switch (historyWindow) {
      case 'hour':
        setIntervalValue('minute');
        startDate = new Date(Date.now() - 3600 * 1000).toISOString();
        break;
      case 'day':
        setIntervalValue('hour');
        startDate = new Date(Date.now() - 86400 * 1000).toISOString().split('T')[0];
        break;
      case 'week':
        setIntervalValue('day');
        startDate = new Date(Date.now() - 7 * 86400 * 1000).toISOString().split('T')[0];
        break;
      case 'month':
        setIntervalValue('day');
        startDate = new Date(Date.now() - 30 * 86400 * 1000).toISOString().split('T')[0];
        break;
      case 'year':
        setIntervalValue('day');
        startDate = new Date(Date.now() - 30 * 86400 * 1000 * 12).toISOString().split('T')[0];
        break;
      default:
        startDate = '2024-01-01';
    }

    const requestBody = {
      ticker,
      session_token: sessionToken,
      start_date: startDate,
      end_date: endDate,
      interval,
      limit: 100,
    };

    try {
      const response = await fetch('https://tradeagently.dev/get-ticker-aggregates', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
      });
      const data = await response.json();

      if (data.status === 'Success' && Array.isArray(data.ticker_info)) {
        const validData = data.ticker_info.filter((item: any) => item.close !== undefined);
        setHistoricalData({
          labels: validData.map((item: any) =>
            historyWindow === 'hour'
              ? new Date(item.t).toLocaleString('en-US', { hour: 'numeric', minute: 'numeric', hour12: true })
              : new Date(item.t).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
          ),
          prices: validData.map((item: any) => item.close),
        });
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
  
    // Ensure the selected client is set for both Fund Manager and Fund Administrator
    if ((isFundManager || clients.length === 1) && !selectedClient) {
      Alert.alert('Error', 'Client information is missing. Please select a client or try again.');
      return;
    }
  
    if (!selectedStock?.symbol) {
      Alert.alert('Error', 'No stock selected for purchase.');
      return;
    }
  
    const requestBody = {
      session_token: sessionToken,
      client_id: isFundManager ? selectedClient : clients.length === 1 ? clients[0].client_id : undefined,
      ticker: selectedStock.symbol, // Use the selected stock's symbol directly
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
      console.log("No price entered");
      return;
    }
  
    if (!alertTicker) {
      Alert.alert('Error', 'No stock selected for the price alert.');
      console.log("No ticker selected");
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
          `Price alert set for ${alertTicker} at ${alertPrice} USD. Notifications will be sent when the price is reached.`
        );
        setPriceAlertModalVisible(false);
        setAlertPrice(''); // Reset alertPrice after success
        setAlertTicker(null); // Reset alertTicker after success
      } else {
        Alert.alert('Error', data.message || 'Failed to set the price alert.');
      }
    } catch (error) {
      console.error('Error setting price alert:', error);
      Alert.alert('Error', 'An error occurred while setting the price alert.');
    }
  };
  
  
  
  

  const renderStockItem = ({ item }: { item: Stock }) => (
    <View style={styles.stockItem}>
      <TouchableOpacity
        onPress={async () => {
          setLoading(true);
          try {
            const response = await fetch('https://tradeagently.dev/get-ticker-info', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ ticker: item.symbol, session_token: sessionToken }),
            });
  
            const data = await response.json();
  
            if (data.status === 'Success') {
              setSelectedStock(data.ticker_info);
            } else {
              Alert.alert('Error', 'Failed to fetch stock details.');
            }
          } catch (err) {
            console.error('Error fetching stock details:', err);
            Alert.alert('Error', 'An error occurred while fetching stock details.');
          } finally {
            setLoading(false);
          }
        }}
      >
        <View>
          <Text style={styles.stockSymbol}>{item.symbol}</Text>
          <Text>{item.company_name}</Text>
          <Text style={styles.priceText}>
            {item.price} {item.currency}
          </Text>
        </View>
      </TouchableOpacity>
  
      {/* Button Container for Buy and Price Alert */}
      <View style={styles.buttonContainer}>
        {/* Buy Button */}
        <TouchableOpacity
          style={styles.buyButton}
          onPress={async () => {
            setLoading(true);
            try {
              const response = await fetch('https://tradeagently.dev/get-ticker-info', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ticker: item.symbol, session_token: sessionToken }),
              });
  
              const data = await response.json();
  
              if (data.status === 'Success') {
                setSelectedStock(data.ticker_info);
                setBuyModalVisible(true);
              } else {
                Alert.alert('Error', 'Failed to fetch stock details.');
              }
            } catch (err) {
              console.error('Error fetching stock details:', err);
              Alert.alert('Error', 'An error occurred while fetching stock details.');
            } finally {
              setLoading(false);
            }
          }}
        >
          <Text style={styles.buyButtonText}>Buy</Text>
        </TouchableOpacity>
  
        {/* Price Alert Button */}
        <TouchableOpacity
          style={styles.alertButton}
          onPress={() => {
            setAlertTicker(item.symbol);
            setPriceAlertModalVisible(true);
          }}
        >
          <Text style={styles.alertButtonText}>Set Price Alert</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
  

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
        <View style={styles.stockDetails}>
          <Text style={styles.stockSymbol}>
            {selectedStock.symbol} - {selectedStock.company_name}
          </Text>
          <Text style={styles.description}>{selectedStock.company_description}</Text>
          <Text style={styles.detailText}>
            Current Price: {selectedStock.close_price} {selectedStock.currency}
          </Text>
          <Text style={styles.detailText}>
            Open: {selectedStock.open_price}, High: {selectedStock.high_price}, Low: {selectedStock.low_price}
          </Text>
          <Text style={styles.detailText}>Volume: {selectedStock.volume}</Text>
          <Text style={styles.detailText}>Employee Count: {selectedStock.employee_count}</Text>
          <Text style={styles.sectionTitle}>Historical Data</Text>
          <Picker selectedValue={historyWindow} onValueChange={(value) => setHistoryWindow(value)}>
            <Picker.Item label="Last Hour" value="hour" />
            <Picker.Item label="Last Day" value="day" />
            <Picker.Item label="Last Week" value="week" />
            <Picker.Item label="Last Month" value="month" />
            <Picker.Item label="Last Year" value="year" />
          </Picker>
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
          <TouchableOpacity
            style={[styles.modalButton, styles.cancelButton]}
            onPress={() => {
              setSelectedStock(null);
              setSearchResults([]);
            }}
          >
            <Text style={styles.modalButtonText}>Back</Text>
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
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Buy {selectedStock?.symbol}</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="Enter amount in USD"
              keyboardType="numeric"
              value={buyAmount}
              onChangeText={setBuyAmount}
            />
            {isFundManager && (
              <View style={styles.dropdownContainer}>
                <Text style={styles.modalLabel}>Select Client:</Text>
                <View style={styles.pickerWrapper}>
                  <Picker
                    selectedValue={selectedClient}
                    onValueChange={(value) => setSelectedClient(value)}
                    style={styles.picker}
                  >
                    <Picker.Item label="Select a client" value={null} />
                    {clients.map((client) => (
                      <Picker.Item key={client.client_id} label={client.client_name} value={client.client_id} />
                    ))}
                  </Picker>
                </View>
              </View>
            )}
            <TouchableOpacity style={styles.modalButton} onPress={handleBuyStock}>
              <Text style={styles.modalButtonText}>Confirm Purchase</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.modalButton, styles.cancelButton]}
              onPress={() => setBuyModalVisible(false)}
            >
              <Text style={styles.modalButtonText}>Cancel</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
  
      {/* Price Alert Modal */}
      <Modal visible={priceAlertModalVisible} transparent={true} animationType="slide">
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Set Price Alert</Text>
            <Text style={styles.modalText}>
              Enter the alert price for {selectedStock?.symbol}:
            </Text>
            <TextInput
              style={styles.modalInput}
              placeholder="Alert Price"
              keyboardType="numeric"
              value={alertPrice}
              onChangeText={setAlertPrice}
            />
            <TouchableOpacity style={styles.modalButton} onPress={confirmPriceAlert}>
              <Text style={styles.modalButtonText}>Confirm Alert</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.modalButton, styles.cancelButton]}
              onPress={() => setPriceAlertModalVisible(false)}
            >
              <Text style={styles.modalButtonText}>Cancel</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </ScrollView>
  );
  
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    width: '100%',
  },
  searchInput: {
    height: 40,
    borderColor: 'gray',
    borderWidth: 1,
    marginBottom: 16,
    paddingHorizontal: 8,
  },
  errorText: {
    color: 'red',
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  stockItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#ddd',
  },
  stockSymbol: {
    fontWeight: 'bold',
  },
  priceText: {
    color: 'green',
    fontWeight: 'bold',
  },
  stockDetails: {
    padding: 16,
  },
  description: {
    marginVertical: 8,
    fontStyle: 'italic',
  },
  detailText: {
    marginVertical: 4,
    fontSize: 16,
    color: '#333',
  },
  buyButton: {
    backgroundColor: '#28a745',
    padding: 10,
    borderRadius: 5,
  },
  buyButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
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
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  modalInput: {
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 5,
    paddingHorizontal: 10,
    height: 40,
    marginBottom: 10,
  },
  modalButton: {
    backgroundColor: '#007bff',
    padding: 10,
    borderRadius: 5,
    marginBottom: 10,
  },
  cancelButton: {
    backgroundColor: '#FF494B',
    paddingVertical: 5,
    paddingHorizontal: 10,
    borderRadius: 5,
    alignSelf: 'center',
    marginTop: 20,
    width: '40%',
  },
  modalButtonText: {
    color: '#333',
    fontWeight: 'bold',
    fontSize: 14,
    textAlign: 'center',
  },
  chartContainer: {
    height: 300,
    width: '85%',
    alignSelf: 'center',
    marginVertical: 20,
  },
  modalLabel: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 8,
    color: '#333',
  },
  dropdownContainer: {
    marginBottom: 16, // Add spacing below the dropdown
  },
  pickerWrapper: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 5,
    overflow: 'hidden', // Ensures the picker looks neat
    backgroundColor: '#fff', // Optional for a better contrast
  },
  picker: {
    height: 40,
    paddingHorizontal: 10,
    color: '#333', // Text color
  },
  modalText: {
    fontSize: 16, // Adjust the font size as needed
    color: '#333', // Dark gray text color
    marginBottom: 10, // Spacing below the text
    textAlign: 'center', // Center align the text
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 10,
  },
  alertButton: {
    backgroundColor: '#FFAA00', // Yellow color for the alert button
    padding: 10,
    borderRadius: 5,
    marginLeft: 10, // Spacing between buttons
  },
  alertButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
  
});

export default StockSearch;
