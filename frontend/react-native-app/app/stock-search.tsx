import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, ScrollView } from 'react-native';
import { useSessionToken } from '../components/userContext';
import { Line } from 'react-chartjs-2';
import { Picker } from '@react-native-picker/picker';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler, TimeScale } from 'chart.js';
import 'chartjs-adapter-date-fns';

// Register necessary components for Chart.js
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

const StockSearch: React.FC = () => {
  const [topStocks, setTopStocks] = useState<Stock[]>([]);
  const [searchResults, setSearchResults] = useState<Stock[]>([]);
  const [selectedStock, setSelectedStock] = useState<StockDetails | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sessionToken] = useSessionToken();
  const [historicalData, setHistoricalData] = useState<{ labels: string[], prices: number[] }>({ labels: [], prices: [] });
  const [historyWindow, setHistoryWindow] = useState<string>('month');
  const [interval, setIntervalValue] = useState<string>('day');

  useEffect(() => {
    fetchTopStocks();
  }, []);

  useEffect(() => {
    if (selectedStock) {
      fetchStockAggregates(selectedStock.symbol);
    }
  }, [interval, selectedStock]);

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

  const handleSearch = async (query: string) => {
    setSearchQuery(query);
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const requestBody = {
        search_query: query,
        limit: 5,
        session_token: sessionToken || '',
        show_price: true,
      };
      console.log('Sending search request:', requestBody);

      const response = await fetch('https://tradeagently.dev/text-search-stock', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
      });

      const data = await response.json();
      console.log('Search response:', data);
      if (data.status === 'Success') {
        setSearchResults(data.ticker_details);
        setSelectedStock(null);
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

  const fetchStockDetails = async (ticker: string) => {
    setLoading(true);
    setError(null);
    try {
      const requestBody = { ticker, session_token: sessionToken || '' };
      console.log('Fetching stock details with request:', requestBody);

      const response = await fetch('https://tradeagently.dev/get-ticker-info', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
      });

      const data = await response.json();
      console.log('Stock details response:', data);
      if (data.status === 'Success') {
        setSelectedStock(data.ticker_info);
        fetchStockAggregates(data.ticker_info.symbol);
      } else {
        setError(data.message || 'Failed to fetch stock details');
      }
    } catch (error) {
      console.error('Error fetching stock details:', error);
      setError('Failed to fetch stock details');
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
    console.log('Fetching stock aggregates with request:', requestBody);

    try {
      const response = await fetch('https://tradeagently.dev/get-ticker-aggregates', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
      });
      const data = await response.json();
      console.log('Stock aggregates response:', data);

      if (data.status === 'Success' && Array.isArray(data.ticker_info)) {
        const validData = data.ticker_info.filter((item: any) => item.close !== undefined);
        setHistoricalData({
          labels: validData.map((item: any) =>
            historyWindow === 'hour'
              ? new Date(item.t).toLocaleString('en-US', { hour: 'numeric', minute: 'numeric', hour12: true })
              : new Date(item.t).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
          ),
          prices: validData.map((item: any) => item.close), // Using close price for the chart
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

  const renderStockItem = ({ item }: { item: Stock }) => (
    <TouchableOpacity style={styles.stockItem} onPress={() => fetchStockDetails(item.symbol)}>
      <Text style={styles.stockSymbol}>{item.symbol}</Text>
      <Text>{item.company_name}</Text>
      <Text style={styles.priceText}>{item.price} {item.currency}</Text>
    </TouchableOpacity>
  );

  return (
    <ScrollView style={styles.container}>
      <TextInput
        style={styles.searchInput}
        placeholder="Search for a stock..."
        value={searchQuery}
        onChangeText={handleSearch}
      />
      {error && <Text style={styles.errorText}>{error}</Text>}
      <Text style={styles.sectionTitle}>Top Stocks</Text>
      {loading && <ActivityIndicator size="large" color="#0000ff" />}
      {selectedStock ? (
        <View style={styles.stockDetails}>
          <Text style={styles.stockSymbol}>{selectedStock.symbol} - {selectedStock.company_name}</Text>
          <Text style={styles.description}>{selectedStock.company_description}</Text>
          <Text style={styles.detailText}>Current Price: {selectedStock.close_price} {selectedStock.currency}</Text>
          <Text style={styles.detailText}>Open: {selectedStock.open_price}, High: {selectedStock.high_price}, Low: {selectedStock.low_price}</Text>
          <Text style={styles.detailText}>Volume: {selectedStock.volume}</Text>
          <Text style={styles.detailText}>Employee Count: {selectedStock.employee_count}</Text>
          <Text style={styles.sectionTitle}>Historical Data</Text>
          <Picker
            selectedValue={historyWindow}
            onValueChange={(value) => setHistoryWindow(value)}
          >
            <Picker.Item label="Last Hour" value="hour" />
            <Picker.Item label="Last Day" value="day" />
            <Picker.Item label="Last Week" value="week" />
            <Picker.Item label="Last Month" value="month" />
            <Picker.Item label="Last Year" value="year" />
          </Picker>
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
              plugins: {
                title: {
                  display: true,
                  text: 'Price History',
                },
              },
              scales: {
                x: {
                  type: historyWindow === 'hour' ? 'time' : 'category',
                  time: {
                    unit: historyWindow === 'hour' ? 'minute' : 'day',
                  },
                },
              },
            }}
          />
        </View>
      ) : (
        <FlatList
          data={searchQuery ? searchResults : topStocks}
          renderItem={renderStockItem}
          keyExtractor={(item) => item.symbol}
        />
      )}
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
  },
  picker: {
    marginVertical: 16,
  },
});

export default StockSearch;
