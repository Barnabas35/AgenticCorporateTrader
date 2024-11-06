import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  TextInput,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  ScrollView,
  Image,
} from 'react-native';
import RNPickerSelect from 'react-native-picker-select';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const CryptoSearch: React.FC = () => {
  const [cryptoSymbol, setCryptoSymbol] = useState('');
  const [filteredCryptoList, setFilteredCryptoList] = useState<any[]>([]);
  const [selectedCrypto, setSelectedCrypto] = useState<any>({ id: 'bitcoin', name: 'Bitcoin' });
  const [currency, setCurrency] = useState('usd');
  const [priceData, setPriceData] = useState<number[]>([]);
  const [labels, setLabels] = useState<string[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [timeInterval, setTimeInterval] = useState<string>('7');

  const cryptoList = [
    { id: 'bitcoin', name: 'Bitcoin', symbol: 'btc' },
    { id: 'ethereum', name: 'Ethereum', symbol: 'eth' },
    { id: 'dogecoin', name: 'Doge', symbol: 'doge' },
    { id: 'ripple', name: 'Ripple', symbol: 'xrp' },
    { id: 'solana', name: 'Solana', symbol: 'sol' },
    { id: 'litecoin', name: 'Litecoin', symbol: 'ltc' },
    { id: 'tron', name: 'Tron', symbol: 'trx' },
    { id: 'tether', name: 'Tether', symbol: 'usdt' },
    { id: 'cardano', name: 'Cardano', symbol: 'ada' },
  ];

  useEffect(() => {
    if (cryptoSymbol) {
      const filteredList = cryptoList.filter(
        (coin) =>
          coin.name.toLowerCase().includes(cryptoSymbol.toLowerCase()) ||
          coin.symbol.toLowerCase().includes(cryptoSymbol.toLowerCase())
      );
      setFilteredCryptoList(filteredList);
    } else {
      setFilteredCryptoList([]);
    }
  }, [cryptoSymbol]);

  const fetchCryptoData = async (crypto: any) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(
        `https://api.coingecko.com/api/v3/coins/${crypto.id}/market_chart?vs_currency=${currency}&days=${timeInterval}&interval=daily`
      );
      if (!response.ok) throw new Error('Failed to fetch data');
      const data = await response.json();
      const prices = data.prices.map((pricePoint: any) => pricePoint[1]);
      const timeLabels = data.prices.map((pricePoint: any) =>
        new Date(pricePoint[0]).toLocaleDateString('en-US', {
          month: 'short',
          day: 'numeric',
        })
      );
      setPriceData(prices);
      setLabels(timeLabels);
    } catch (err: any) {
      setError(err.message || 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCryptoData(selectedCrypto);
  }, [selectedCrypto, timeInterval]);

  const renderCryptoItem = ({ item }: { item: any }) => (
    <TouchableOpacity
      style={styles.cryptoItem}
      onPress={() => {
        setSelectedCrypto(item);
        setCryptoSymbol(item.name);
        setFilteredCryptoList([]);
      }}
    >
      <Image
        source={{ uri: `https://assets.coingecko.com/coins/images/${item.id}/small.png` }}
        style={styles.cryptoLogo}
      />
      <View>
        <Text style={styles.cryptoName}>{item.name}</Text>
        <Text style={styles.cryptoSymbol}>{item.symbol.toUpperCase()}</Text>
      </View>
    </TouchableOpacity>
  );

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Cryptocurrency Price Tracker</Text>
      <TextInput
        style={styles.searchInput}
        placeholder="Search cryptocurrency..."
        value={cryptoSymbol}
        onChangeText={setCryptoSymbol}
      />
      {filteredCryptoList.length > 0 && (
        <FlatList
          data={filteredCryptoList}
          keyExtractor={(item) => item.id}
          renderItem={renderCryptoItem}
        />
      )}

      <View style={styles.pickerContainer}>
        <RNPickerSelect
          onValueChange={(value) => setCurrency(value)}
          items={[
            { label: 'USD', value: 'usd' },
            { label: 'EUR', value: 'eur' },
          ]}
          value={currency}
          placeholder={{ label: 'Select currency', value: null }}
        />
        <RNPickerSelect
          onValueChange={(value) => setTimeInterval(value)}
          items={[
            { label: '1 Day', value: '1' },
            { label: '7 Days', value: '7' },
            { label: '30 Days', value: '30' },
            { label: '90 Days', value: '90' },
            { label: '1 Year', value: '365' },
          ]}
          value={timeInterval}
          placeholder={{ label: 'Select time range', value: '7' }}
        />
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#0000ff" style={styles.loading} />
      ) : error ? (
        <Text style={styles.errorText}>{error}</Text>
      ) : (
        <View style={styles.chartContainer}>
          <Line
            data={{
              labels: labels,
              datasets: [
                {
                  label: `Price in ${currency.toUpperCase()}`,
                  data: priceData,
                  borderColor: 'rgba(75, 192, 192, 1)',
                  backgroundColor: 'rgba(75, 192, 192, 0.2)',
                  borderWidth: 1,
                  fill: true,
                  tension: 0.1,
                },
              ],
            }}
            options={{
              responsive: true,
              maintainAspectRatio: false,
              plugins: {
                legend: { position: 'top' },
                title: {
                  display: true,
                  text: `${selectedCrypto.name} Price Over the Last ${timeInterval} Days`,
                },
              },
              scales: {
                x: { title: { display: true, text: 'Date' } },
                y: { title: { display: true, text: `Price (${currency.toUpperCase()})` } },
              },
            }}
          />
        </View>
      )}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    backgroundColor: '#f9f9f9',
    width: '100%',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 16,
    textAlign: 'center',
  },
  searchInput: {
    height: 50,
    borderColor: '#ccc',
    borderWidth: 1,
    paddingHorizontal: 10,
    borderRadius: 5,
    fontSize: 18,
    marginBottom: 10,
  },
  pickerContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  cryptoItem: {
    flexDirection: 'row',
    padding: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#ddd',
    backgroundColor: '#fff',
    marginBottom: 10,
  },
  cryptoLogo: {
    width: 30,
    height: 30,
    marginRight: 10,
  },
  cryptoName: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  cryptoSymbol: {
    fontSize: 16,
    color: '#555',
  },
  chartContainer: {
    width: '100%',
    padding: 32,
    borderRadius: 5,
    backgroundColor: '#fff',
    shadowColor: '#000',
    shadowOpacity: 0.1,
    shadowRadius: 8,
    marginVertical: 10,
  },
  errorText: {
    color: 'red',
    textAlign: 'center',
    marginVertical: 10,
  },
  loading: {
    marginVertical: 20,
  },
});

export default CryptoSearch;
