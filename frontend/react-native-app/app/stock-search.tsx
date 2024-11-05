// src/app/StockSearch.tsx

import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
} from 'react-native';
import { useSessionToken } from '../components/userContext';
import { LineChart } from 'react-native-chart-kit';

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
        // Simulating historical data for the chart
        setHistoricalData([data.ticker_info.close_price + 5, data.ticker_info.close_price - 2, data.ticker_info.close_price + 3, data.ticker_info.close_price - 1, data.ticker_info.close_price + 4]); // Sample data
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

  const renderStockItem = ({ item }: { item: Stock }) => (
    <TouchableOpacity
      style={styles.stockItem}
      onPress={() => fetchStockDetails(item.symbol)}
    >
      <Text style={styles.stockSymbol}>{item.symbol}</Text>
      <Text>{item.company_name}</Text>
      <Text>{item.price} {item.currency}</Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <TextInput
        style={styles.searchInput}
        placeholder="Search for a stock..."
        value={searchQuery}
        onChangeText={handleSearch}
      />

      {error && <Text style={styles.errorText}>{error}</Text>}
      {loading && <ActivityIndicator size="large" color="#0000ff" />}

      {selectedStock ? (
        <View style={styles.stockDetails}>
          <Text style={styles.stockSymbol}>{selectedStock.symbol} - {selectedStock.company_name}</Text>
          <Text style={styles.description}>{selectedStock.company_description}</Text>
          <Text style={styles.detailText}>Current Price: {selectedStock.close_price} {selectedStock.currency}</Text>
          <Text style={styles.detailText}>Open: {selectedStock.open_price}, High: {selectedStock.high_price}, Low: {selectedStock.low_price}</Text>
          <Text style={styles.detailText}>Volume: {selectedStock.volume}</Text>
          <Text style={styles.detailText}>Employee Count: {selectedStock.employee_count}</Text>
          
          {/* Line Chart for historical prices */}
          <LineChart
            data={{
              labels: ['1', '2', '3', '4', '5'],
              datasets: [
                {
                  data: historicalData,
                },
              ],
            }}
            width={340} // from react-native
            height={220}
            yAxisLabel="$"
            yAxisInterval={1} // optional, defaults to 1
            chartConfig={{
              backgroundColor: '#ffffff',
              backgroundGradientFrom: '#ffffff',
              backgroundGradientTo: '#ffffff',
              decimalPlaces: 2, // optional, defaults to 2
              color: (opacity = 1) => `rgba(0, 128, 255, ${opacity})`,
              labelColor: (opacity = 1) => `rgba(0, 0, 0, ${opacity})`,
              style: {
                borderRadius: 16,
              },
              propsForDots: {
                r: '6',
                strokeWidth: '2',
                stroke: '#ffa726',
              },
            }}
            style={{
              marginVertical: 8,
              borderRadius: 16,
            }}
          />
          
          <TouchableOpacity onPress={() => setSelectedStock(null)}>
            <Text style={styles.backButton}>Back to Results</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <>
          <Text style={styles.sectionTitle}>Top Stocks</Text>
          <FlatList
            data={searchQuery ? searchResults : topStocks}
            renderItem={renderStockItem}
            keyExtractor={(item) => item.symbol}
          />
        </>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    backgroundColor: '#f9f9f9',
    width: '50%'
  },
  searchInput: {
    height: 50,
    borderColor: '#ccc',
    borderWidth: 1,
    paddingHorizontal: 10,
    marginBottom: 10,
    borderRadius: 5,
    fontSize: 18,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    marginVertical: 10,
  },
  stockItem: {
    padding: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#ddd',
    borderRadius: 5,
    backgroundColor: '#fff',
    marginBottom: 10,
  },
  stockSymbol: {
    fontSize: 20,
    fontWeight: 'bold',
  },
  stockDetails: {
    padding: 16,
    borderRadius: 5,
    backgroundColor: '#fff',
    shadowColor: '#000',
    shadowOpacity: 0.1,
    shadowRadius: 8,
    marginVertical: 10,
  },
  detailText: {
    fontSize: 18,
    marginVertical: 4,
  },
  description: {
    fontSize: 16,
    marginVertical: 4,
    color: '#555',
  },
  errorText: {
    color: 'red',
    marginBottom: 10,
  },
  backButton: {
    color: 'blue',
    marginTop: 10,
    textAlign: 'center',
    fontSize: 18,
  },
});

export default StockSearch;
