import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet } from 'react-native';
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
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const BitcoinChart: React.FC = () => {
  const [bitcoinData, setBitcoinData] = useState<number[][] | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Fetch Bitcoin price data from CoinGecko API
    const fetchBitcoinData = async () => {
      try {
        const response = await fetch(
          'https://api.coingecko.com/api/v3/coins/bitcoin/market_chart?vs_currency=usd&days=7&interval=daily'
        );

        if (!response.ok) {
          throw new Error('Failed to fetch Bitcoin data');
        }

        const data = await response.json();

        // data.prices is an array of [timestamp, price] arrays
        setBitcoinData(data.prices);
      } catch (err: any) {
        console.error('Error fetching Bitcoin data:', err);
        setError(err.message || 'An error occurred');
      } finally {
        setLoading(false);
      }
    };

    fetchBitcoinData();
  }, []);

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <Text>Loading Bitcoin Price Chart...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.errorContainer}>
        <Text>Error: {error}</Text>
      </View>
    );
  }

  // Preparing data for the chart
  const chartData = {
    labels: bitcoinData
      ? bitcoinData.map((point) =>
          new Date(point[0]).toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
          })
        )
      : [],
    datasets: [
      {
        label: 'Bitcoin Price (USD)',
        data: bitcoinData ? bitcoinData.map((point) => point[1]) : [],
        borderColor: 'rgba(255, 99, 132, 1)',
        backgroundColor: 'rgba(255, 99, 132, 0.2)',
        fill: true,
        tension: 0.1,
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top' as const,
      },
      title: {
        display: true,
        text: 'Bitcoin Price Over the Last 7 Days',
      },
    },
  };

  return (
    <View style={styles.chartContainer}>
      <Line data={chartData} options={options} />
    </View>
  );
};

const styles = StyleSheet.create({
  chartContainer: {
    width: '50%',
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

export default BitcoinChart;
