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
import { Picker } from '@react-native-picker/picker'; // Updated import
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

// Register necessary components for Chart.js
ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

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
  const [historicalData, setHistoricalData] = useState<number[]>([]); // Placeholder for historical prices
  const [interval, setInterval] = useState<string>('day'); // Default interval

  useEffect(() => {
    fetchTopStocks();
  }, []);

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
        setSelectedStock(null); // Clear previous stock details
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
    const startDate = '2024-01-01'; // Placeholder date
    const endDate = '2024-12-31'; // Placeholder date

    try {
      const response = await fetch('https://tradeagently.dev/get-ticker-aggregates', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ticker,
          session_token: sessionToken || '',
          start_date: startDate,
          end_date: endDate,
          interval, // Interval is dynamic based on user selection
          limit: 100,
        }),
      });

      const data = await response.json();
      if (data.status === 'Success' && data.aggregates) {
        // Prepare data for chart
        const labels = data.aggregates.map((item: any) => new Date(item.t).toLocaleDateString());
        const prices = data.aggregates.map((item: any) => item.c); // Use closing price for the graph

        setHistoricalData(prices);
      } else {
        setError('Failed to fetch stock aggregates');
      }
    } catch (error) {
      console.error('Error fetching stock aggregates:', error);
      setError('Failed to fetch stock aggregates');
    } finally {
      setLoading(false);
    }
  };

  const renderStockItem = ({ item }: { item: Stock }) => (
    <TouchableOpacity
      style={styles.stockItem}
      onPress={() => fetchStockDetails(item.symbol)}
    >
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
            <Picker.Item label="Hourly" value="hour" />
            <Picker.Item label="Daily" value="day" />
            <Picker.Item label="Weekly" value="week" />
            <Picker.Item label="Monthly" value="month" />
          </Picker>

          {/* Line Chart for historical prices */}
          <View style={styles.chartContainer}>
            <Line
              data={{
                labels: historicalData.length > 0 ? historicalData.map((_, index) => index + 1) : [], // Placeholder labels
                datasets: [
                  {
                    label: `Price in ${selectedStock.currency}`,
                    data: historicalData,
                    borderColor: 'rgba(75, 192, 192, 1)',
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    borderWidth: 1,
                    fill: true,
                  },
                ],
              }}
              options={{
                responsive: true,
                plugins: {
                  title: {
                    display: true,
                    text: `${selectedStock.symbol} Price History`,
                  },
                },
                scales: {
                  x: {
                    type: 'linear',
                    position: 'bottom',
                  },
                },
              }}
            />
          </View>
        </View>
      ) : (
        <FlatList
          data={searchQuery.trim() ? searchResults : topStocks}
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
    padding: 10,
    backgroundColor: '#f4f4f4',
    width: '100%',
  },
  searchInput: {
    padding: 10,
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 5,
    marginBottom: 10,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginVertical: 10,
  },
  stockItem: {
    padding: 15,
    backgroundColor: '#fff',
    borderRadius: 5,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOpacity: 0.1,
    shadowRadius: 5,
  },
  stockSymbol: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  priceText: {
    fontSize: 16,
    color: '#007bff',
  },
  stockDetails: {
    padding: 20,
    backgroundColor: '#fff',
    borderRadius: 10,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOpacity: 0.1,
    shadowRadius: 5,
  },
  description: {
    marginVertical: 10,
    fontSize: 16,
    color: '#666',
  },
  detailText: {
    marginVertical: 4,
    fontSize: 16,
  },
  picker: {
    height: 50,
    width: '100%',
  },
  chartContainer: {
    width: '100%',
    height: 300,
    marginVertical: 20,
  },
  errorText: {
    color: 'red',
    textAlign: 'center',
    marginVertical: 10,
  },
});

export default StockSearch;
