import React, { useEffect, useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, FlatList, TouchableOpacity, Image } from 'react-native';
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

// Register the necessary Chart.js components
ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const CryptoSearch: React.FC = () => {
  const [cryptoSymbol, setCryptoSymbol] = useState('');
  const [filteredCryptoList, setFilteredCryptoList] = useState<any[]>([]);
  const [selectedCrypto, setSelectedCrypto] = useState<any>({ id: 'bitcoin', name: 'Bitcoin' }); // Default is Bitcoin
  const [currency, setCurrency] = useState('usd');
  const [priceData, setPriceData] = useState<number[]>([]);
  const [labels, setLabels] = useState<string[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // New state to handle the selected time interval
  const [timeInterval, setTimeInterval] = useState<string>('7'); // Default is 7 days

  const cryptoList = [
    { id: 'bitcoin', name: 'Bitcoin', symbol: 'btc' },
    { id: 'ethereum', name: 'Ethereum', symbol: 'eth' },
    { id: 'dogecoin', name: 'Doge', symbol: 'doge' },
    { id: 'ripple', name: 'Ripple', symbol: 'xrp' },
    { id: 'solana', name: 'Solana', symbol: 'sol' },
    { id: 'litecoin', name: 'Litecoin', symbol: 'ltc' },
    { id: 'tron', name: 'Tron', symbol: 'trx' },
    { id: 'tether', name: 'tETHER', symbol: 'usdt' },
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

      if (!response.ok) {
        throw new Error('Failed to fetch data');
      }

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
      console.error('Error fetching data:', err);
      setError(err.message || 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  // Refetch data when the selected crypto or time interval changes
  useEffect(() => {
    fetchCryptoData(selectedCrypto);
  }, [selectedCrypto, timeInterval]);

  return (
    <View style={styles.pageContainer}>
      <View style={styles.container}>
        <Text style={styles.title}>Cryptocurrency Price Search</Text>

        <TextInput
          style={styles.input}
          placeholder="Enter Cryptocurrency (e.g., Bitcoin, Ethereum)"
          value={cryptoSymbol}
          onChangeText={setCryptoSymbol}
        />

        {filteredCryptoList.length > 0 && (
          <FlatList
            data={filteredCryptoList}
            keyExtractor={(item) => item.id}
            renderItem={({ item }) => (
              <TouchableOpacity
                style={styles.suggestionItem}
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
                <Text>{item.name}</Text>
              </TouchableOpacity>
            )}
          />
        )}

        {/* Currency Picker */}
        <View style={styles.pickerContainer}>
          <RNPickerSelect
            onValueChange={(value) => setCurrency(value)}
            items={[
              { label: 'USD', value: 'usd' },
              { label: 'EUR', value: 'eur' },
            ]}
            value={currency}
            placeholder={{ label: 'Select a currency', value: null }}
          />
        </View>

        {/* Time Interval Picker */}
        <View style={styles.pickerContainer}>
          <RNPickerSelect
            onValueChange={(value) => setTimeInterval(value)}
            items={[
              { label: '1 Day', value: '1' },
              { label: '7 Days', value: '7' }, // Default
              { label: '30 Days', value: '30' },
              { label: '90 Days', value: '90' },
              { label: '1 Year', value: '365' },
            ]}
            value={timeInterval}
            placeholder={{ label: 'Select a time interval', value: '7' }}
          />
        </View>

        <View style={styles.spacer} />

        <Button title="Fetch Data" onPress={() => fetchCryptoData(selectedCrypto)} />

        <View style={styles.spacer} />

        {loading ? (
          <View style={styles.loadingContainer}>
            <Text>Loading Price Chart...</Text>
          </View>
        ) : error ? (
          <View style={styles.errorContainer}>
            <Text>Error: {error}</Text>
          </View>
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
                  title: { display: true, text: `${selectedCrypto.name} Price Over the Last ${timeInterval} Days` },
                },
                scales: {
                  x: { title: { display: true, text: 'Date' } },
                  y: { title: { display: true, text: `Price (${currency.toUpperCase()})` } },
                },
              }}
            />
          </View>
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  pageContainer: {
    flex: 1,
    backgroundColor: '#f0f0f0', // Light gray background for the whole page
    justifyContent: 'center',
    alignItems: 'center',
    width: '100%',
  },
  container: {
    padding: 20,
    justifyContent: 'center',
    alignItems: 'center',
    width: '60%',
    backgroundColor: '#f0f0f0',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  input: {
    width: '100%',
    height: 40,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 10,
    marginBottom: 15,
  },
  pickerContainer: {
    width: '100%', // Match width with the input
    marginBottom: 20, // Space below the picker
  },
  spacer: {
    height: 20, // Spacer between picker and button
  },
  suggestionItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 10,
  },
  cryptoLogo: {
    width: 24,
    height: 24,
    marginRight: 10,
  },
  chartContainer: {
    width: '100%',
    height: 300,
    backgroundColor: '#ffffff',
    borderRadius: 10,
    padding: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  loadingContainer: {
    justifyContent: 'center',
    alignItems: 'center',
    height: 300,
  },
  errorContainer: {
    justifyContent: 'center',
    alignItems: 'center',
    height: 300,
    backgroundColor: '#ffe6e6',
    borderRadius: 10,
    padding: 10,
  },
});

export default CryptoSearch;
