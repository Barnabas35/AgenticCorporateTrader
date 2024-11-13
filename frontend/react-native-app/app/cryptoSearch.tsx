import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, ScrollView } from 'react-native';
import { useSessionToken } from '../components/userContext';
import { Line } from 'react-chartjs-2';
import { Picker } from '@react-native-picker/picker';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler, TimeScale } from 'chart.js';
import 'chartjs-adapter-date-fns';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler, TimeScale);

interface Crypto { symbol: string; name: string; price: number; }
interface CryptoDetails extends Crypto { high: number; low: number; volume: number; description: string; }

const CryptoSearch: React.FC = () => {
  const [topCryptos, setTopCryptos] = useState<Crypto[]>([]);
  const [searchResults, setSearchResults] = useState<Crypto[]>([]);
  const [selectedCrypto, setSelectedCrypto] = useState<CryptoDetails | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sessionToken] = useSessionToken();
  const [historicalData, setHistoricalData] = useState<{ labels: string[], prices: number[] }>({ labels: [], prices: [] });
  const [historyWindow, setHistoryWindow] = useState<string>('month'); 
  const [interval, setInterval] = useState<string>('1d');

  useEffect(() => { fetchTopCryptos(); }, []);
  useEffect(() => { if (selectedCrypto) fetchCryptoAggregates(selectedCrypto.symbol); }, [interval, selectedCrypto]);

  const fetchTopCryptos = async (limit = 10) => {
    setLoading(true); setError(null);
    try {
      const response = await fetch(`https://tradeagently.dev/get-top-cryptos?limit=${limit}`);
      const data = await response.json();
      data.status === 'Success' ? setTopCryptos(data.crypto_details) : setError('Failed to fetch top cryptos');
    } catch { setError('Failed to fetch top cryptos'); } finally { setLoading(false); }
  };

  const handleSearch = async (query: string) => {
    setSearchQuery(query);
    if (!query.trim()) return setSearchResults([]);
  
    setLoading(true);
    setError(null);
    console.log("Search query:", query);
  
    try {
      console.log("Sending request to search API...");
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
  
      console.log("Response status:", response.status);
      const data = await response.json();
      console.log("API response data:", data);
  
      if (data.status === 'Success' && data.crypto_details) {
        setSearchResults(data.crypto_details);
        console.log("Search results:", data.crypto_details);
      } else {
        setError('No matching cryptocurrencies found');
      }
    } catch (error) {
      console.error("Error fetching data:", error);
      setError('Error searching cryptocurrencies');
    } finally {
      setLoading(false);
      console.log("Loading state set to false");
    }
  };
  
  

  const fetchCryptoDetails = async (symbol: string) => {
    setLoading(true); setError(null);
    try {
      const response = await fetch('https://tradeagently.dev/get-crypto-info', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ crypto: symbol, session_token: sessionToken || '' }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        setSelectedCrypto(data.crypto_info); fetchCryptoAggregates(symbol);
      } else { setError(data.message || 'Failed to fetch crypto details'); }
    } catch { setError('Failed to fetch crypto details'); } finally { setLoading(false); }
  };

  const fetchCryptoAggregates = async (symbol: string) => {
    setLoading(true); setError(null);
    let startDate = ''; let endDate = new Date().toISOString().split('T')[0];
    switch (historyWindow) {
      case 'hour': setInterval('1m'); startDate = new Date(Date.now() - 3600 * 1000).toISOString(); break;
      case 'day': setInterval('1h'); startDate = new Date(Date.now() - 86400 * 1000).toISOString().split('T')[0]; break;
      case 'week': setInterval('1d'); startDate = new Date(Date.now() - 7 * 86400 * 1000).toISOString().split('T')[0]; break;
      case 'month': setInterval('1d'); startDate = new Date(Date.now() - 30 * 86400 * 1000).toISOString().split('T')[0]; break;
      case 'year': setInterval('1mo'); startDate = new Date(Date.now() - 30 * 86400 * 1000 * 12).toISOString().split('T')[0]; break;
      default: startDate = '2024-01-01';
    }

    try {
      const response = await fetch('https://tradeagently.dev/get-crypto-aggregates', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ crypto: symbol, session_token: sessionToken, start_date: startDate, end_date: endDate, interval }),
      });
      const data = await response.json();
      const validData = data.crypto_aggregates.filter((item: any) => item.close !== undefined);
      setHistoricalData({
        labels: validData.map((item: any) =>
          historyWindow === 'hour' ? new Date(item.date).toLocaleString('en-US', { hour: 'numeric', minute: 'numeric', hour12: true }) :
            new Date(item.date).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
        ),
        prices: validData.map((item: any) => item.close),
      });
    } catch { setError('Failed to fetch crypto aggregates'); } finally { setLoading(false); }
  };

  const renderCryptoItem = ({ item }: { item: Crypto }) => (
    <TouchableOpacity style={styles.cryptoItem} onPress={() => fetchCryptoDetails(item.symbol)}>
      <Text style={styles.cryptoSymbol}>{item.symbol}</Text><Text>{item.name}</Text><Text style={styles.priceText}>{item.price} USD</Text>
    </TouchableOpacity>
  );

  return (
    <ScrollView style={styles.container}>
      <TextInput style={styles.searchInput} placeholder="Search for a crypto..." value={searchQuery} onChangeText={handleSearch} />
      {error && <Text style={styles.errorText}>{error}</Text>}
      <Text style={styles.sectionTitle}>Top Cryptos</Text>
      {loading && <ActivityIndicator size="large" color="#0000ff" />}
      {selectedCrypto ? (
        <View style={styles.cryptoDetails}>
          <Text style={styles.cryptoSymbol}>{selectedCrypto.symbol} - {selectedCrypto.name}</Text>
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
          <Line
            data={{ labels: historicalData.labels, datasets: [{ label: 'Price', data: historicalData.prices, borderColor: 'rgba(75,192,192,1)', borderWidth: 1, fill: false }] }}
            options={{ responsive: true, plugins: { title: { display: true, text: 'Price History' } }, scales: { x: { type: historyWindow === 'hour' ? 'time' : 'category', time: { unit: historyWindow === 'hour' ? 'minute' : 'day' } } } }}
          />
        </View>
      ) : (
        <FlatList data={searchQuery ? searchResults : topCryptos} renderItem={renderCryptoItem} keyExtractor={(item) => item.symbol} />
      )}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { padding: 10, width: '100%' },
  searchInput: { marginVertical: 10, padding: 10, borderColor: '#ccc', borderWidth: 1, borderRadius: 5 },
  errorText: { color: 'red', marginBottom: 10 },
  sectionTitle: { fontSize: 20, fontWeight: 'bold', marginVertical: 10 },
  cryptoItem: { flexDirection: 'row', justifyContent: 'space-between', padding: 10, borderBottomWidth: 1, borderBottomColor: '#ccc' },
  cryptoSymbol: { fontSize: 18, fontWeight: 'bold' },
  cryptoDetails: { padding: 10, borderColor: '#ccc', borderWidth: 1, borderRadius: 5 },
  description: { fontSize: 14, marginBottom: 5 },
  priceText: { fontSize: 16, color: '#1a73e8', fontWeight: 'bold' },
});

export default CryptoSearch;
