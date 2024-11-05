// src/app/stock-details.tsx

import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';
import { useLocation } from 'react-router-dom';
import { StockInfo } from '../components/stockdetails'; // Import the StockInfo type

const StockDetails: React.FC = () => {
  const { state } = useLocation();
  const { ticker } = state || {};
  const [stockInfo, setStockInfo] = useState<StockInfo | null>(null);
  const [aggregates, setAggregates] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const sessionToken = 'your-session-token'; // Replace with actual session token

  useEffect(() => {
    const fetchStockInfo = async () => {
      setLoading(true);
      try {
        const response = await fetch('/get-ticker-info', {
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
        const response = await fetch('/get-ticker-aggregates', {
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

  return (
    <View style={styles.container}>
      {loading ? (
        <Text>Loading...</Text>
      ) : (
        <>
          {stockInfo && (
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
        </>
      )}
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
});

export default StockDetails;
