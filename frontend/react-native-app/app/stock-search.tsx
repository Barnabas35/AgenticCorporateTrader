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
} from 'chart.js';

// Register necessary components for Chart.js
ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler);

interface Stock {
  symbol: string;
  company_name: string;
  price: number;
  currency: string;
}

interface StockDetails {
  symbol: string;
  company_name: string;
  price: number;
  currency: string;
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
  const [interval, setInterval] = useState<string>('day'); // Default interval

  useEffect(() => {
    fetchTopStocks();
  }, []);

  useEffect(() => {
    if (selectedStock) {
      fetchStockAggregates(selectedStock.symbol); // Fetch when interval changes
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
      const response = await fetch('https://tradeagently.dev/get-ticker-info', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ticker, session_token: sessionToken || '' }),
      });

      const data = await response.json();
      if (data.status === 'Success') {
        setSelectedStock(data.ticker_info);
        fetchStockAggregates(data.ticker_info.symbol); // Fetch historical data
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

  // Fetch historical data with interval handling
  const fetchStockAggregates = async (ticker: string) => {
    setLoading(true);
    setError(null);

    // Adjust the date range dynamically based on the selected interval
    let startDate = '';
    let endDate = new Date().toISOString().split('T')[0]; // Today's date (YYYY-MM-DD)

    switch (interval) {
      case 'hour':
        setInterval('minute');
        startDate = new Date(Date.now() - 3600 * 1000).toISOString().split('T')[0]; // Last hour
        break;
      case 'day':
        setInterval('hour');
        startDate = new Date(Date.now() - 86400 * 1000).toISOString().split('T')[0]; // Last 24 hours
        break;
      case 'week':
        setInterval('day');
        startDate = new Date(Date.now() - 7 * 86400 * 1000).toISOString().split('T')[0]; // Last 7 days
        break;
      case 'month':
        setInterval('week');
        startDate = new Date(Date.now() - 30 * 86400 * 1000).toISOString().split('T')[0]; // Last 30 days
        break;
        case 'year':
          setInterval('month');
          startDate = new Date(Date.now() - 30 * 86400 * 1000 * 12).toISOString().split('T')[0]; // Last Year
          break;
      default:
        startDate = '2024-01-01'; // Default to start of the year if invalid interval
    }

    try {
      const response = await fetch('https://tradeagently.dev/get-ticker-aggregates', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ticker,
          session_token: sessionToken,
          start_date: startDate,
          end_date: endDate,
          interval,
          limit: 100,
        }),
      });

      const data = await response.json();
      if (data.status === 'Success' && Array.isArray(data.ticker_info)) {
        const validData = data.ticker_info.filter((item: any) => item.close !== undefined);

        const labels = validData.map((item: any) =>
          new Date(item.timestamp).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
        );
        const prices = validData.map((item: any) => item.close);

        setHistoricalData({ labels, prices }); // Store both labels and prices
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

          {/* Interval Picker */}
          <Picker
            selectedValue={interval}
            style={styles.picker}
            onValueChange={(itemValue) => setInterval(itemValue)}
          >
            <Picker.Item label="Daily" value="day" />
            <Picker.Item label="Weekly" value="week" />
            <Picker.Item label="Monthly" value="month" />
          </Picker>

          {/* Line Chart for historical prices */}
          <View style={styles.chartContainer}>
            <Line
              data={{
                labels: historicalData.labels,
                datasets: [
                  {
                    label: 'Price',
                    data: historicalData.prices,
                    borderColor: 'rgba(75,192,192,1)',
                    backgroundColor: 'rgba(75,192,192,0.2)',
                    fill: true,
                  },
                ],
              }}
              options={{
                responsive: true,
                scales: {
                  x: {
                    type: 'category',
                  },
                  y: {
                    beginAtZero: false,
                  },
                },
              }}
            />
          </View>
        </View>
      ) : (
        <FlatList
          data={searchResults.length > 0 ? searchResults : topStocks}
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
  chartContainer: {
    marginTop: 16,
    marginLeft: 10,
    marginRight: 10
  },
});

export default StockSearch;
