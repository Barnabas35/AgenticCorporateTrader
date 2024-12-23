import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  TextInput,
  FlatList,
  TouchableOpacity,
  Alert,
  StyleSheet,
  Modal,
} from 'react-native';
import { Picker } from '@react-native-picker/picker';
import { useSessionToken } from '../components/userContext';
import { useNavigate } from 'react-router-dom';

interface AccountingData {
  asset_growth?: string;
  asset_liquidity?: string;
  asset_profitability?: string;
}

interface AssetReportData {
  profit?: number;
  total_usd_invested?: number;
}

const ClientManagement: React.FC = () => {
  const [sessionToken] = useSessionToken();
  const navigate = useNavigate();

  // -----------------------------
  // CLIENT & ASSET STATES
  // -----------------------------
  const [clients, setClients] = useState<any[]>([]);
  const [selectedClientId, setSelectedClientId] = useState<string | null>(null);
  const [selectedClientName, setSelectedClientName] = useState<string>('');
  const [assets, setAssets] = useState<string[]>([]);
  const [assetDetails, setAssetDetails] = useState<{ [ticker: string]: number }>({});

  // -----------------------------
  // TABS & BASIC FORM
  // -----------------------------
  const [activeTab, setActiveTab] = useState<string>('Manage Assets');
  const [newClientName, setNewClientName] = useState<string>('');
  const [market, setMarket] = useState<string>('stocks');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // -----------------------------
  // PURCHASE & SELL STATES
  // -----------------------------
  const [balance, setBalance] = useState<number | null>(null);
  const [buyModalVisible, setBuyModalVisible] = useState(false);
  const [purchaseTicker, setPurchaseTicker] = useState('');
  const [purchaseAmount, setPurchaseAmount] = useState<string>('');

  const [sellModalVisible, setSellModalVisible] = useState(false);
  const [sellTicker, setSellTicker] = useState('');
  const [sellQuantity, setSellQuantity] = useState<string>('');
  const [sellMaxQuantity, setSellMaxQuantity] = useState<number>(0);

  // -----------------------------
  // ACCOUNTING & ASSET REPORT
  // -----------------------------
  const [accountingModalVisible, setAccountingModalVisible] = useState(false);
  const [accountingTicker, setAccountingTicker] = useState('');
  const [accountingData, setAccountingData] = useState<AccountingData | null>(null);

  const [reportModalVisible, setReportModalVisible] = useState<boolean>(false);
  const [reportData, setReportData] = useState<AssetReportData | null>(null);

  // -----------------------------
  // REDIRECT IF NOT LOGGED IN
  // -----------------------------
  if (!sessionToken) {
    navigate('/login');
    return null;
  }

  // -----------------------------
  // FETCH CLIENT LIST
  // -----------------------------
  const fetchClientList = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch('https://tradeagently.dev/get-client-list', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        setClients(data.clients);
      } else {
        setError('Failed to fetch clients.');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unexpected error occurred.');
    } finally {
      setLoading(false);
    }
  };

  // -----------------------------
  // ADD CLIENT
  // -----------------------------
  const addClient = async () => {
    if (!newClientName.trim()) {
      Alert.alert('Error', 'Client name cannot be empty.');
      return;
    }
    try {
      const response = await fetch('https://tradeagently.dev/add-client', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          client_name: newClientName,
        }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        setClients((prev) => [
          ...prev,
          { client_name: newClientName, client_id: data.client_id ?? Date.now().toString() },
        ]);
        setNewClientName('');
        Alert.alert('Success', 'Client added successfully!');
      } else {
        Alert.alert('Error', 'Failed to add client.');
      }
    } catch (err) {
      Alert.alert('Error', 'An error occurred while adding the client.');
    }
  };

  // -----------------------------
  // REMOVE CLIENT
  // -----------------------------
  const removeClient = async (clientName: string) => {
    try {
      const response = await fetch('https://tradeagently.dev/remove-client', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          client_name: clientName,
        }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        setClients((prev) => prev.filter((c) => c.client_name !== clientName));
        Alert.alert('Success', 'Client removed successfully!');
        if (selectedClientId === clientName) {
          setSelectedClientId(null);
        }
      } else {
        Alert.alert('Error', 'Failed to remove client.');
      }
    } catch (err) {
      Alert.alert('Error', 'An error occurred while removing the client.');
    }
  };

  // -----------------------------
  // FETCH USER ASSETS
  // -----------------------------
  const fetchUserAssets = async (clientId: string) => {
    try {
      setAssets([]);
      setAssetDetails({});
      const response = await fetch('https://tradeagently.dev/get-user-assets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          client_id: clientId,
          market,
        }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        const tickerSymbols = data.ticker_symbols || [];
        setAssets(tickerSymbols);
        if (tickerSymbols.length > 0) {
          await fetchAssetDetails(tickerSymbols, clientId);
        }
      } else {
        setAssets([]);
        setAssetDetails({});
      }
    } catch (err) {
      setAssets([]);
      setAssetDetails({});
      console.error('Error fetching user assets:', err);
    }
  };

  // -----------------------------
  // FETCH ASSET DETAILS
  // -----------------------------
  const fetchAssetDetails = async (tickers: string[], clientId: string) => {
    const details: { [ticker: string]: number } = {};
    for (const tk of tickers) {
      try {
        const resp = await fetch('https://tradeagently.dev/get-asset', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            session_token: sessionToken,
            market,
            ticker: tk,
            client_id: clientId,
          }),
        });
        const assetData = await resp.json();
        if (assetData.status === 'Success') {
          details[tk] = assetData.total_asset_quantity;
        } else {
          details[tk] = 0;
        }
      } catch {
        details[tk] = 0;
      }
    }
    setAssetDetails(details);
  };

  // -----------------------------
  // FETCH BALANCE (for purchase)
  // -----------------------------
  const fetchBalance = async () => {
    if (!sessionToken) return;
    try {
      const response = await fetch('https://tradeagently.dev/get-balance', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        setBalance(data.balance);
      } else {
        setBalance(null);
      }
    } catch (err) {
      console.error('Error fetching balance:', err);
      setBalance(null);
    }
  };

  // -----------------------------
  // OPEN/CLOSE PURCHASE MODAL
  // -----------------------------
  const openPurchaseModal = async (ticker: string = '') => {
    if (!selectedClientId) return;
    await fetchBalance(); // update balance each time
    setPurchaseTicker(ticker);
    setPurchaseAmount('');
    setBuyModalVisible(true);
  };

  const confirmPurchase = async () => {
    if (!selectedClientId || !purchaseTicker || !purchaseAmount) {
      Alert.alert('Error', 'Missing info to purchase.');
      return;
    }
    const usd = parseFloat(purchaseAmount);
    if (isNaN(usd) || usd <= 0) {
      Alert.alert('Error', 'Invalid USD amount.');
      return;
    }
    try {
      const resp = await fetch('https://tradeagently.dev/purchase-asset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          usd_quantity: usd,
          market,
          ticker: purchaseTicker,
          client_id: selectedClientId,
        }),
      });
      const data = await resp.json();
      if (data.status === 'Success') {
        Alert.alert('Success', `Purchased ${purchaseTicker} for $${usd} (Client: ${selectedClientName}).`);
        setBuyModalVisible(false);
        fetchUserAssets(selectedClientId);
      } else {
        Alert.alert('Error', data.message || 'Failed to purchase asset.');
      }
    } catch (err) {
      Alert.alert('Error', 'An error occurred while purchasing the asset.');
    }
  };

  // -----------------------------
  // OPEN/CLOSE SELL MODAL
  // -----------------------------
  const openSellModal = (ticker: string) => {
    if (!selectedClientId) return;
    setSellTicker(ticker);
    const amt = assetDetails[ticker] || 0;
    setSellMaxQuantity(amt);
    setSellQuantity('');
    setSellModalVisible(true);
  };

  const confirmSell = async (sellAll: boolean = false) => {
    if (!selectedClientId || !sellTicker) return;
    let qty: number;
    if (sellAll) {
      qty = sellMaxQuantity;
    } else {
      qty = parseFloat(sellQuantity);
      if (isNaN(qty) || qty <= 0) {
        Alert.alert('Error', 'Invalid sell quantity.');
        return;
      }
    }
    if (qty <= 0) {
      Alert.alert('Error', 'Quantity must be positive.');
      return;
    }
    try {
      const resp = await fetch('https://tradeagently.dev/sell-asset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          asset_quantity: qty,
          market,
          ticker: sellTicker,
          client_id: selectedClientId,
        }),
      });
      const data = await resp.json();
      if (data.status === 'Success') {
        Alert.alert('Success', `Sold ${qty} of ${sellTicker} (Client: ${selectedClientName}).`);
        setSellModalVisible(false);
        fetchUserAssets(selectedClientId);
      } else {
        Alert.alert('Error', data.message || 'Failed to sell asset.');
      }
    } catch (err) {
      Alert.alert('Error', 'An error occurred while selling the asset.');
    }
  };

  // -----------------------------
  // OPEN/CLOSE ACCOUNTING MODAL
  // -----------------------------
  const openAccountingModal = async (ticker: string) => {
    if (!selectedClientId) return;

    // 1) Show the modal immediately so we know it opens
    setAccountingModalVisible(true);

    // 2) Clear previous data
    setAccountingData(null);
    setAccountingTicker(ticker);

    // 3) Fetch the new data
    try {
      const resp = await fetch('https://tradeagently.dev/get-ai-accounting', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          market,
          ticker,
          client_id: selectedClientId,
        }),
      });
      const data = await resp.json();

      setAccountingData({
        asset_growth: data.asset_growth,
        asset_liquidity: data.asset_liquidity,
        asset_profitability: data.asset_profitability,
      });
    } catch (err) {
      Alert.alert('Error', 'An error occurred fetching AI Accounting.');
    }
  };

  // -----------------------------
  // GET ASSET REPORT
  // -----------------------------
  const getAssetReport = async (ticker: string) => {
    if (!selectedClientId || !ticker) {
      Alert.alert('Error', 'Invalid client or ticker for asset report.');
      return;
    }
    setReportData(null);
    try {
      const resp = await fetch('https://tradeagently.dev/get-asset-report', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          market,
          client_id: selectedClientId,
          ticker,
        }),
      });
      const data = await resp.json();
      if (data.status === 'Success') {
        setReportData({
          profit: data.profit,
          total_usd_invested: data.total_usd_invested,
        });
        setReportModalVisible(true);
      } else {
        Alert.alert('Error', data.message || 'Failed to fetch asset report.');
      }
    } catch (err) {
      Alert.alert('Error', 'An error occurred while fetching the asset report.');
    }
  };

  // -----------------------------
  // DOWNLOAD CSV
  // -----------------------------
  const handleDownloadReports = async () => {
    try {
      const resp = await fetch('https://tradeagently.dev/download-asset-reports', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const data = await resp.json();
      if (data.status === 'success' && data.url) {
        const fileResp = await fetch(data.url);
        const blob = await fileResp.blob();

        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'asset_reports.csv';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } else {
        Alert.alert('Error', 'No CSV download URL returned.');
      }
    } catch (err) {
      Alert.alert('Error', 'An error occurred while downloading the CSV file.');
    }
  };

  // -----------------------------
  // useEffect INIT
  // -----------------------------
  useEffect(() => {
    if (sessionToken) {
      fetchClientList();
    } else {
      setError('Session token is missing.');
    }
  }, [sessionToken]);

  useEffect(() => {
    if (selectedClientId) {
      fetchUserAssets(selectedClientId);
    }
  }, [market, selectedClientId]);

  // -----------------------------
  // RENDER
  // -----------------------------
  return (
    <View style={styles.pageContainer}>
      <View style={styles.centeredContainer}>
        <View style={styles.headerRow}>
          <Text style={styles.title}>Client Management</Text>
          <TouchableOpacity style={[styles.roundButton, { backgroundColor: '#333' }]} onPress={handleDownloadReports}>
            <Text style={styles.buttonText}>Download All Asset Report</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.tabsContainer}>
          <TouchableOpacity
            style={[styles.tab, activeTab === 'Manage Assets' && styles.activeTab]}
            onPress={() => setActiveTab('Manage Assets')}
          >
            <Text style={styles.tabText}>Manage Assets</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.tab, activeTab === 'Add Clients' && styles.activeTab]}
            onPress={() => setActiveTab('Add Clients')}
          >
            <Text style={styles.tabText}>Add Clients</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.tab, activeTab === 'Delete Clients' && styles.activeTab]}
            onPress={() => setActiveTab('Delete Clients')}
          >
            <Text style={styles.tabText}>Delete Clients</Text>
          </TouchableOpacity>
        </View>

        {activeTab === 'Manage Assets' && (
          <>
            {loading ? (
              <Text>Loading...</Text>
            ) : error ? (
              <Text style={styles.errorText}>Error: {error}</Text>
            ) : (
              <FlatList
                data={clients}
                keyExtractor={(item) => item.client_id}
                renderItem={({ item }) => (
                  <TouchableOpacity
                    style={[
                      styles.clientItem,
                      selectedClientId === item.client_id && styles.selectedClientItem,
                    ]}
                    onPress={() => {
                      setSelectedClientId(item.client_id);
                      setSelectedClientName(item.client_name);
                    }}
                  >
                    <Text>{item.client_name}</Text>
                  </TouchableOpacity>
                )}
              />
            )}

            {selectedClientId && (
              <>
                <Text style={styles.sectionTitle}>Manage Assets for {selectedClientName}</Text>

                <Text style={styles.label}>Select Market:</Text>
                <View style={styles.pickerWrapper}>
                  <Picker
                    selectedValue={market}
                    style={{ height: 40, width: 150 }}
                    onValueChange={(val) => setMarket(val)}
                  >
                    <Picker.Item label="Stocks" value="stocks" />
                    <Picker.Item label="Crypto" value="crypto" />
                  </Picker>
                </View>

                <TouchableOpacity
                  style={[styles.roundButton, { backgroundColor: 'green', marginVertical: 10 }]}
                  onPress={() => openPurchaseModal('')}
                >
                  <Text style={styles.buttonText}>Purchase a New Asset</Text>
                </TouchableOpacity>

                <Text style={styles.sectionTitle}>Assets</Text>
                {assets.length === 0 ? (
                  <Text style={styles.noAssetsText}>
                    This client does not own any {market} assets.
                  </Text>
                ) : (
                  <FlatList
                    data={assets}
                    keyExtractor={(item, idx) => `${item}-${idx}`}
                    renderItem={({ item: ticker }) => (
                      <View style={styles.assetRow}>
                        <Text style={styles.assetItem}>
                          {ticker}: {assetDetails[ticker]?.toFixed(5) ?? '0.00000'}
                        </Text>

                        {/* Buy More */}
                        <TouchableOpacity
                          style={[styles.roundButton, { backgroundColor: '#007bff', marginRight: 5 }]}
                          onPress={() => openPurchaseModal(ticker)}
                        >
                          <Text style={styles.buttonText}>Buy More</Text>
                        </TouchableOpacity>

                        {/* Sell */}
                        <TouchableOpacity
                          style={[styles.roundButton, { backgroundColor: '#FFAA00', marginRight: 5 }]}
                          onPress={() => openSellModal(ticker)}
                        >
                          <Text style={styles.buttonText}>Sell</Text>
                        </TouchableOpacity>

                        {/* Get (Asset) Report */}
                        <TouchableOpacity
                          style={[styles.roundButton, { backgroundColor: '#666', marginRight: 5 }]}
                          onPress={() => getAssetReport(ticker)}
                        >
                          <Text style={styles.buttonText}>Get Report</Text>
                        </TouchableOpacity>

                        {/* View AI Accounting */}
                        <TouchableOpacity
                          style={[styles.roundButton, { backgroundColor: 'purple' }]}
                          onPress={() => openAccountingModal(ticker)}
                        >
                          <Text style={styles.buttonText}>View Accounting</Text>
                        </TouchableOpacity>
                      </View>
                    )}
                  />
                )}
              </>
            )}
          </>
        )}

        {activeTab === 'Add Clients' && (
          <View style={styles.addClientContainer}>
            <Text style={styles.label}>Enter new client name:</Text>
            <TextInput
              style={styles.input}
              placeholder="Client Name"
              value={newClientName}
              onChangeText={setNewClientName}
            />
            <TouchableOpacity
              style={[styles.roundButton, { backgroundColor: 'green', marginTop: 10 }]}
              onPress={addClient}
            >
              <Text style={styles.buttonText}>Add Client</Text>
            </TouchableOpacity>
          </View>
        )}

        {activeTab === 'Delete Clients' && (
          <FlatList
            data={clients}
            keyExtractor={(item) => item.client_id}
            renderItem={({ item }) => (
              <View style={styles.clientItem}>
                <Text>{item.client_name}</Text>
                <TouchableOpacity onPress={() => removeClient(item.client_name)}>
                  <Text style={{ color: 'red', fontSize: 18 }}>❌</Text>
                </TouchableOpacity>
              </View>
            )}
          />
        )}
      </View>

      {/* ---------- PURCHASE MODAL ---------- */}
      <Modal visible={buyModalVisible} transparent={true} animationType="slide">
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Purchase Asset</Text>
            <Text style={styles.modalSubtitle}>Client: {selectedClientName}</Text>
            {balance !== null && (
              <Text style={{ marginVertical: 5 }}>Your Balance: ${balance.toFixed(2)}</Text>
            )}

            <TextInput
              style={styles.input}
              placeholder="Ticker (e.g. BTC)"
              value={purchaseTicker}
              onChangeText={setPurchaseTicker}
            />
            <TextInput
              style={styles.input}
              placeholder="USD Amount"
              keyboardType="numeric"
              value={purchaseAmount}
              onChangeText={setPurchaseAmount}
            />

            <View style={styles.modalButtonRow}>
              <TouchableOpacity
                style={[styles.roundButton, { backgroundColor: '#007bff' }]}
                onPress={confirmPurchase}
              >
                <Text style={styles.buttonText}>Confirm</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.roundButton, { backgroundColor: '#888' }]}
                onPress={() => setBuyModalVisible(false)}
              >
                <Text style={styles.buttonText}>Cancel</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      {/* ---------- SELL MODAL ---------- */}
      <Modal visible={sellModalVisible} transparent={true} animationType="slide">
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Sell Asset</Text>
            <Text style={styles.modalSubtitle}>Client: {selectedClientName}</Text>
            <Text style={{ marginVertical: 5 }}>Asset: {sellTicker}</Text>
            <Text>Owned Quantity: {sellMaxQuantity.toFixed(5)}</Text>

            <TextInput
              style={styles.input}
              placeholder="Quantity to sell"
              keyboardType="numeric"
              value={sellQuantity}
              onChangeText={setSellQuantity}
            />
            <View style={styles.modalButtonRow}>
              <TouchableOpacity
                style={[styles.roundButton, { backgroundColor: '#ff9100' }]}
                onPress={() => confirmSell(false)}
              >
                <Text style={styles.buttonText}>Sell</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.roundButton, { backgroundColor: '#b200b2' }]}
                onPress={() => confirmSell(true)}
              >
                <Text style={styles.buttonText}>Sell All</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.roundButton, { backgroundColor: '#888' }]}
                onPress={() => setSellModalVisible(false)}
              >
                <Text style={styles.buttonText}>Cancel</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      {/* ---------- ACCOUNTING MODAL ---------- */}
      <Modal visible={accountingModalVisible} transparent={true} animationType="slide">
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>AI Accounting</Text>
            <Text style={styles.modalSubtitle}>Client: {selectedClientName}</Text>
            <Text style={{ marginVertical: 5 }}>Asset: {accountingTicker}</Text>

            {/* Minimal text if accountingData is still null */}
            {!accountingData ? (
              <Text style={{ marginVertical: 10 }}>Fetching data...</Text>
            ) : (
              <View style={{ marginVertical: 10 }}>
                <Text>Growth: {accountingData.asset_growth || ''}</Text>
                <Text>Liquidity: {accountingData.asset_liquidity || ''}</Text>
                <Text>Profitability: {accountingData.asset_profitability || ''}</Text>
              </View>
            )}

            <TouchableOpacity
              style={[styles.roundButton, { backgroundColor: 'purple', marginTop: 20, alignSelf: 'stretch' }]}
              onPress={() => setAccountingModalVisible(false)}
            >
              <Text style={styles.buttonText}>Close</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* ---------- ASSET REPORT MODAL (Get Report) ---------- */}
      <Modal visible={reportModalVisible} transparent={true} animationType="slide">
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Asset Report</Text>
            <Text style={styles.modalSubtitle}>Client: {selectedClientName}</Text>

            {reportData ? (
              <>
                <Text>Profit: {reportData.profit}</Text>
                <Text>Total USD Invested: {reportData.total_usd_invested}</Text>
              </>
            ) : (
              <Text style={{ marginTop: 10 }}>Loading or no data...</Text>
            )}

            <TouchableOpacity
              style={[styles.roundButton, { backgroundColor: '#666', marginTop: 20, alignSelf: 'stretch' }]}
              onPress={() => setReportModalVisible(false)}
            >
              <Text style={styles.buttonText}>Close</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
};

// -----------------------------
// STYLES
// -----------------------------
const styles = StyleSheet.create({
  pageContainer: {
    flex: 1,
    backgroundColor: '#f0f0f0',
    alignItems: 'center',
    padding: 10,
    width: '100%',
  },
  centeredContainer: {
    backgroundColor: '#fff',
    borderRadius: 12,
    width: '75%',
    padding: 20,
  },
  headerRow: {
    flexDirection: 'row',
    width: '100%',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
  },
  tabsContainer: {
    flexDirection: 'row',
    marginTop: 10,
    marginBottom: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#ccc',
  },
  tab: {
    flex: 1,
    paddingVertical: 10,
    alignItems: 'center',
    borderBottomWidth: 2,
    borderBottomColor: 'transparent',
  },
  activeTab: {
    borderBottomColor: '#007bff',
  },
  tabText: {
    fontSize: 16,
    color: '#333',
  },
  errorText: {
    color: 'red',
    marginBottom: 10,
  },
  clientItem: {
    paddingVertical: 10,
    paddingHorizontal: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#ccc',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  selectedClientItem: {
    backgroundColor: '#d2f7d2',
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginTop: 20,
    marginBottom: 10,
  },
  label: {
    marginTop: 10,
    fontWeight: '600',
  },
  pickerWrapper: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
    width: 160,
    marginVertical: 5,
  },
  roundButton: {
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderRadius: 8,
    minWidth: 110,
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 2,
  },
  buttonText: {
    color: '#fff',
    fontWeight: '600',
  },
  assetRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'center',
    marginVertical: 5,
  },
  assetItem: {
    fontSize: 15,
    width: 120,
  },
  input: {
    width: '100%',
    height: 40,
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
    paddingHorizontal: 8,
    marginVertical: 5,
  },
  addClientContainer: {
    marginTop: 20,
  },
  noAssetsText: {
    marginTop: 10,
    fontStyle: 'italic',
    color: '#555',
  },
  modalBackground: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContainer: {
    width: '35%',
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 10,
    alignItems: 'flex-start',
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 6,
  },
  modalSubtitle: {
    fontStyle: 'italic',
    marginBottom: 10,
  },
  modalButtonRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    width: '100%',
    marginTop: 10,
  },
});

export default ClientManagement;
