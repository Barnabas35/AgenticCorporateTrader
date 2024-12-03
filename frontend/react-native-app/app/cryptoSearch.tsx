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
  const [interval, setIntervalValue] = useState<string>('1d');
  const [buyModalVisible, setBuyModalVisible] = useState(false);
  const [buyUsdQuantity, setBuyUsdQuantity] = useState<string>('');
  const [buyTicker, setBuyTicker] = useState<string>('');

  useEffect(() => {
    fetchTopCryptos();
  }, []);

  useEffect(() => {
    if (selectedCrypto) {
      fetchCryptoAggregates(selectedCrypto.symbol);
    }
  }, [interval, historyWindow, selectedCrypto]);

  const fetchTopCryptos = async (limit = 10) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`https://tradeagently.dev/get-top-cryptos?limit=${limit}`);
      const data = await response.json();
      data.status === 'Success'
        ? setTopCryptos(data.crypto_details)
        : setError('Failed to fetch top cryptos');
    } catch {
      setError('Failed to fetch top cryptos');
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
    let startDate = '';
    const endDate = new Date().toISOString().split('T')[0];
    switch (historyWindow) {
      case 'hour':
        setIntervalValue('1m');
        startDate = new Date(Date.now() - 3600 * 1000).toISOString();
        break;
      case 'day':
        setIntervalValue('1h');
        startDate = new Date(Date.now() - 86400 * 1000).toISOString().split('T')[0];
        break;
      case 'week':
        setIntervalValue('1d');
        startDate = new Date(Date.now() - 7 * 86400 * 1000).toISOString().split('T')[0];
        break;
      case 'month':
        setIntervalValue('1d');
        startDate = new Date(Date.now() - 30 * 86400 * 1000).toISOString().split('T')[0];
        break;
      case 'year':
        setIntervalValue('1mo');
        startDate = new Date(Date.now() - 30 * 86400 * 1000 * 12).toISOString().split('T')[0];
        break;
      default:
        startDate = '2024-01-01';
    }

    try {
      const response = await fetch('https://tradeagently.dev/get-crypto-aggregates', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          crypto: symbol,
          session_token: sessionToken,
          start_date: startDate,
          end_date: endDate,
          interval,
        }),
      });
      const data = await response.json();
      const validData = data.crypto_aggregates.filter((item: any) => item.close !== undefined);
      setHistoricalData({
        labels: validData.map((item: any) =>
          historyWindow === 'hour'
            ? new Date(item.date).toLocaleString('en-US', {
                hour: 'numeric',
                minute: 'numeric',
                hour12: true,
              })
            : new Date(item.date).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
              })
        ),
        prices: validData.map((item: any) => item.close),
      });
    } catch {
      setError('Failed to fetch crypto aggregates');
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

  const handleBuy = (ticker: string) => {
    setBuyTicker(ticker);
    setBuyModalVisible(true);
  };

  const confirmBuy = async () => {
    if (!buyUsdQuantity || isNaN(parseFloat(buyUsdQuantity))) {
      Alert.alert('Error', 'Please enter a valid USD amount.');
      return;
    }

    console.log(sessionToken)
    console.log(buyUsdQuantity)
    console.log(buyTicker)
    try {
      const response = await fetch('https://tradeagently.dev/purchase-asset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          usd_quantity: parseFloat(buyUsdQuantity),
          market: 'crypto',
          ticker: buyTicker,
        }),
      });

      const data = await response.json();
      console.log(data)
      if (data.status === 'Success') {
        Alert.alert('Success', `Successfully purchased ${buyUsdQuantity} USD of ${buyTicker}.`);
        setBuyModalVisible(false);
        setBuyUsdQuantity('');
      } else {
        Alert.alert('Error', 'Failed to complete the purchase.');
      }
    } catch (error) {
      Alert.alert('Error', 'An error occurred while processing the purchase.');
    }
  };

  const renderCryptoItem = ({ item }: { item: Crypto }) => (
    <View style={styles.cryptoItem}>
      <TouchableOpacity onPress={() => fetchCryptoDetails(item.symbol)}>
        <Text style={styles.cryptoSymbol}>{item.symbol}</Text>
        <Text>{item.name}</Text>
        <Text style={styles.priceText}>{item.price} USD</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.buyButton} onPress={() => handleBuy(item.symbol)}>
        <Text style={styles.buyButtonText}>Buy</Text>
      </TouchableOpacity>
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
          <Text style={styles.cryptoSymbol}>
            {selectedCrypto.symbol} - {selectedCrypto.name}
          </Text>
          <Text style={styles.description}>Market Cap: {selectedCrypto.volume}</Text>
          <Text style={styles.description}>Volume: {selectedCrypto.volume}</Text>
          <Text style={styles.description}>High: {selectedCrypto.high}</Text>
          <Text style={styles.description}>Low: {selectedCrypto.low}</Text>
          <Text style={styles.description}>{selectedCrypto.description}</Text>
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
            style={[styles.backButton]}
            onPress={() => {
              setSelectedCrypto(null);
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
      <Modal visible={buyModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Buy Asset</Text>
            <Text style={styles.modalText}>Enter the amount in USD to buy for {buyTicker}:</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="USD Amount"
              keyboardType="numeric"
              value={buyUsdQuantity}
              onChangeText={setBuyUsdQuantity}
            />
            <View style={styles.modalButtonContainer}>
              <TouchableOpacity style={styles.modalButton} onPress={confirmBuy}>
                <Text style={styles.modalButtonText}>Confirm Purchase</Text>
              </TouchableOpacity>
              <View style={{ width: 10 }} />
              <TouchableOpacity
                style={[styles.modalButton, styles.modalCancelButton]}
                onPress={() => setBuyModalVisible(false)}
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
  sectionTitle: { fontSize: 20, fontWeight: 'bold', marginBottom: 8 },
  cryptoItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#ddd',
  },
  cryptoSymbol: { fontWeight: 'bold' },
  priceText: { color: 'green', fontWeight: 'bold' },
  buyButton: { backgroundColor: '#007bff', padding: 10, borderRadius: 5 },
  buyButtonText: { color: 'white', fontWeight: 'bold' },
  cryptoDetails: { padding: 16 },
  description: { marginVertical: 8, fontStyle: 'italic' },
  modalBackground: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: 'rgba(0, 0, 0, 0.5)' },
  modalContainer: { backgroundColor: 'white', padding: 20, borderRadius: 10, width: '80%' },
  modalTitle: { fontSize: 18, fontWeight: 'bold', marginBottom: 10 },
  modalText: { fontSize: 16, marginBottom: 10 },
  modalInput: { height: 40, borderColor: '#ccc', borderWidth: 1, borderRadius: 5, paddingHorizontal: 10, marginBottom: 10 },
  modalButtonContainer: { flexDirection: 'row', justifyContent: 'space-around', width: '100%' },
  modalButton: { backgroundColor: '#007bff', padding: 10, borderRadius: 5, alignItems: 'center' },
  modalCancelButton: { backgroundColor: '#FF494B' },
  modalButtonText: { color: 'white', fontWeight: 'bold' },
  chartContainer: { height: 300, width: '85%', alignSelf: 'center', marginVertical: 20 },
  backButton: { backgroundColor: '#007bff', padding: 8, borderRadius: 5, alignSelf: 'center', marginVertical: 10 },
  backButtonText: { color: 'white', fontWeight: 'bold' },
});

export default CryptoSearch;
