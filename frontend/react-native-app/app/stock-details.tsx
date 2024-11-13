import React, { useEffect, useState } from 'react';
import { View, Text, TextInput, FlatList, StyleSheet, ActivityIndicator, TouchableOpacity } from 'react-native';
import { useLocation } from 'react-router-dom';
import { StockInfo } from '../components/stockdetails'; // Import the StockInfo type

const StockDetails: React.FC = () => {
  const { state } = useLocation();
  const { ticker } = state || {};
  const [stockInfo, setStockInfo] = useState<StockInfo | null>(null);
  const [aggregates, setAggregates] = useState<any[]>([]);
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [pageIndex, setPageIndex] = useState(1);
  const [totalResults, setTotalResults] = useState(0);
  const sessionToken = 'your-session-token'; // Replace with actual session token

  // API URL
  const API_URL = '/get-ticker-info';
  const AGGREGATES_URL = '/get-ticker-aggregates';
  const SEARCH_URL = '/search-stocks';

  // Fetch stock info and aggregates for the page
  useEffect(() => {
    const fetchStockInfo = async () => {
      setLoading(true);
      try {
        const response = await fetch(API_URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ ticker, session_token: sessionToken }),
        });
        const data = await response.json();
        if (data.status === 'Success') {
          setStockInfo(data.ticker_info);
        } else {
          console.log('Failed to fetch stock info:', data);
        }
      } catch (error) {
        console.error('Error fetching stock info:', error);
      } finally {
        setLoading(false);
      }
    };

    const fetchAggregates = async () => {
      setLoading(true);
      try {
        const response = await fetch(AGGREGATES_URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            ticker,
            session_token: sessionToken,
            start_date: '2024-01-01',
            end_date: '2024-11-01',
            interval: 'day',
            limit: 100,
          }),
        });
        const data = await response.json();
        if (data.status === 'Success') {
          setAggregates(data.aggregates);
        } else {
          console.log('Failed to fetch aggregates:', data);
        }
      } catch (error) {
        console.error('Error fetching ticker aggregates:', error);
      } finally {
        setLoading(false);
      }
    };

    if (ticker) {
      fetchStockInfo();
      fetchAggregates();
    }
  }, [ticker]);

  // Quick search function (typing search)
  const handleSearch = async (query: string) => {
    setSearchQuery(query);
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }

    setLoading(true);
    try {
      // Quick search fetch - limit 5, no price
      const response = await fetch(SEARCH_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          search_query: query,
          limit: 5,
          session_token: sessionToken,
          show_price: false, // Exclude price for quick search
        }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        setSearchResults(data.stocks);
        setTotalResults(data.total_results || 0); // Set total results for pagination
      } else {
        console.log('Failed to fetch search results:', data);
      }
    } catch (error) {
      console.error('Error searching stocks:', error);
    } finally {
      setLoading(false);
    }
  };

  // Function to handle full search (with price and large results)
  const handleFullSearch = async () => {
    setLoading(true);
    try {
      const response = await fetch(SEARCH_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          search_query: searchQuery,
          limit: 150, // Full search limit
          session_token: sessionToken,
          show_price: true, // Show price when submitting search
          page: pageIndex, // Pass current page index
        }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        setSearchResults(data.stocks);
        setTotalResults(data.total_results || 0); // Set total results for pagination
      } else {
        console.log('Failed to fetch search results:', data);
      }
    } catch (error) {
      console.error('Error submitting full search:', error);
    } finally {
      setLoading(false);
    }
  };

  // Function to handle pagination
  const handlePageChange = (newPage: number) => {
    setPageIndex(newPage);
    handleFullSearch(); // Fetch results for the selected page
  };

  return (
    <View style={styles.container}>
      <View style={styles.searchContainer}>
        <TextInput
          style={styles.searchInput}
          placeholder="Search for a stock..."
          value={searchQuery}
          onChangeText={setSearchQuery}
        />
        <TouchableOpacity style={styles.searchButton} onPress={handleFullSearch}>
          <Text style={styles.searchButtonText}>Search</Text>
        </TouchableOpacity>
      </View>

      {loading && <ActivityIndicator size="large" color="#0000ff" />}
      
      {searchQuery && !loading && (
        <>
          <FlatList
            data={searchResults}
            keyExtractor={(item, index) => index.toString()}
            renderItem={({ item }) => (
              <View style={styles.searchItem}>
                <Text>{item.symbol} - {item.name}</Text>
                {item.price && <Text>Price: {item.price}</Text>} {/* Only show price if available */}
              </View>
            )}
          />

          {totalResults > 5 && (
            <View style={styles.paginationContainer}>
              <TouchableOpacity
                disabled={pageIndex === 1}
                onPress={() => handlePageChange(pageIndex - 1)}
              >
                <Text style={styles.paginationButton}>Previous</Text>
              </TouchableOpacity>
              <Text style={styles.pageIndex}>Page {pageIndex}</Text>
              <TouchableOpacity
                disabled={pageIndex * 150 >= totalResults}
                onPress={() => handlePageChange(pageIndex + 1)}
              >
                <Text style={styles.paginationButton}>Next</Text>
              </TouchableOpacity>
            </View>
          )}
        </>
      )}
      
      {stockInfo && !searchQuery && (
        <View>
          <Text style={styles.title}>{stockInfo.company_name} ({stockInfo.symbol})</Text>
          <Text>Description: {stockInfo.company_description}</Text>
          <Text>Price: {stockInfo.close_price} {stockInfo.currency}</Text>
          <Text>Change: {stockInfo.change_percentage}%</Text>
          <Text>Volume: {stockInfo.volume}</Text>
          <Text>Employee Count: {stockInfo.employee_count}</Text>
        </View>
      )}

      <FlatList
        data={aggregates}
        keyExtractor={(item, index) => index.toString()}
        renderItem={({ item }) => (
          <View style={styles.aggregateItem}>
            <Text>Open: {item.o}</Text>
            <Text>Close: {item.c}</Text>
            <Text>High: {item.h}</Text>
            <Text>Low: {item.l}</Text>
            <Text>Volume: {item.v}</Text>
          </View>
        )}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  aggregateItem: {
    padding: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#ccc',
  },
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20,
  },
  searchInput: {
    flex: 1,
    padding: 10,
    backgroundColor: '#f1f1f1',
    borderRadius: 5,
  },
  searchButton: {
    padding: 10,
    backgroundColor: '#007BFF',
    borderRadius: 5,
    marginLeft: 10,
  },
  searchButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
  searchItem: {
    padding: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#ccc',
  },
  paginationContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 10,
  },
  paginationButton: {
    fontSize: 16,
    fontWeight: 'bold',
    marginHorizontal: 10,
    color: '#007BFF',
  },
  pageIndex: {
    fontSize: 18,
    fontWeight: 'bold',
  },
});

export default StockDetails;
