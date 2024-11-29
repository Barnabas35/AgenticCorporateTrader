import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  TextInput,
  ScrollView,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { useSessionToken } from '../components/userContext';

interface SupportTicket {
  issue_subject: string;
  user_id: string;
  issue_description: string;
  issue_status: 'open' | 'resolved';
  ticket_id: string;
  unix_timestamp: number;
}

interface User {
  email: string;
  id: string;
  password: string;
  session_token: string;
  user_type: string;
  username: string;
}

const AdminPage: React.FC = () => {
  const [sessionToken] = useSessionToken();
  const [tickets, setTickets] = useState<SupportTicket[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useState<'tickets' | 'users'>('tickets');
  const [ticketView, setTicketView] = useState<'open' | 'resolved'>('open');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [expandedTicket, setExpandedTicket] = useState<SupportTicket | null>(null);
  const [responseSubject, setResponseSubject] = useState<string>('');
  const [responseBody, setResponseBody] = useState<string>('');

  useEffect(() => {
    fetchSupportTickets();
    fetchUsers();
  }, []);

  const fetchSupportTickets = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch('https://tradeagently.dev/get-support-ticket-list', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        setTickets(data.support_tickets);
      } else {
        setError('Failed to fetch tickets');
      }
    } catch (err) {
      console.error('Error fetching tickets:', err);
      setError('Failed to fetch tickets');
    } finally {
      setLoading(false);
    }
  };

  const resolveTicket = async (ticket: SupportTicket) => {
    if (!responseSubject.trim() || !responseBody.trim()) {
      Alert.alert('Error', 'Please provide a subject and body for the response.');
      return;
    }

    setLoading(true);
    try {
      const response = await fetch('https://tradeagently.dev/resolve-support-ticket', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          ticket_id: ticket.ticket_id,
          response_subject: responseSubject,
          response_body: responseBody,
        }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        Alert.alert('Success', 'Ticket resolved successfully');
        setResponseSubject('');
        setResponseBody('');
        setExpandedTicket(null);
        fetchSupportTickets();
      } else {
        setError('Failed to resolve ticket');
      }
    } catch (err) {
      console.error('Error resolving ticket:', err);
      setError('Failed to resolve ticket');
    } finally {
      setLoading(false);
    }
  };

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
  
    try {
      const response = await fetch('https://tradeagently.dev/get-user-list', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
  
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
  
      const data = await response.json();
  
      if (data.status === 'Success') {
        setUsers(data.user_list); // Update state with user list
      } else {
        setError(data.message || 'Failed to fetch users');
      }
    } catch (err) {
      console.error('Error fetching users:', err);
      setError('Failed to fetch users. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  

  const deleteUser = async (userId: string) => {
    Alert.alert('Confirm', 'Are you sure you want to delete this user?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Delete',
        onPress: async () => {
          setLoading(true);
          try {
            const response = await fetch('https://tradeagently.dev/admin-delete-user', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ session_token: sessionToken, id: userId }),
            });
            const data = await response.json();
            if (data.status === 'Success') {
              Alert.alert('Success', 'User deleted successfully');
              fetchUsers();
            } else {
              setError('Failed to delete user');
            }
          } catch (err) {
            console.error('Error deleting user:', err);
            setError('Failed to delete user');
          } finally {
            setLoading(false);
          }
        },
      },
    ]);
  };

  const filteredUsers = users.filter(
    (user) =>
      user.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <View style={styles.container}>
      {loading && <ActivityIndicator size="large" color="#0000ff" />}
      {error && <Text style={styles.errorText}>{error}</Text>}

      {/* Navigation Buttons */}
      <View style={styles.centerContainer}>
        <View style={styles.navContainer}>
          <TouchableOpacity
            style={[styles.smallButton, view === 'tickets' ? styles.activeButton : {}]}
            onPress={() => setView('tickets')}
          >
            <Text style={styles.buttonText}>Tickets</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.smallButton, view === 'users' ? styles.deleteUsersButton : {}]}
            onPress={() => setView('users')}
          >
            <Text style={styles.buttonText}>Delete Users</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Tickets View */}
      {view === 'tickets' && (
        <View>
          <View style={styles.ticketNavContainer}>
            <TouchableOpacity
              style={[styles.smallButton, ticketView === 'open' ? styles.openTicketButton : {}]}
              onPress={() => setTicketView('open')}
            >
              <Text style={styles.buttonText}>Open Tickets</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.smallButton, ticketView === 'resolved' ? styles.resolvedTicketButton : {}]}
              onPress={() => setTicketView('resolved')}
            >
              <Text style={styles.buttonText}>Resolved Tickets</Text>
            </TouchableOpacity>
          </View>

          {expandedTicket ? (
            <View style={styles.ticketItem}>
              <Text style={styles.ticketTitle}>{expandedTicket.issue_subject}</Text>
              <Text>{expandedTicket.issue_description}</Text>
              <TextInput
                style={styles.responseInput}
                placeholder="Response Subject"
                value={responseSubject}
                onChangeText={setResponseSubject}
              />
              <TextInput
                style={styles.responseInput}
                placeholder="Response Body"
                value={responseBody}
                onChangeText={setResponseBody}
                multiline
              />
              <TouchableOpacity
                style={styles.button}
                onPress={() => resolveTicket(expandedTicket)}
              >
                <Text style={styles.buttonText}>Submit Response</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.smallButton, styles.cancelButton]}
                onPress={() => setExpandedTicket(null)}
              >
                <Text style={styles.buttonText}>Cancel</Text>
              </TouchableOpacity>
            </View>
          ) : (
            <FlatList
              data={tickets.filter((ticket) => ticket.issue_status === ticketView)}
              renderItem={({ item }) => (
                <View style={styles.ticketItem}>
                  <Text style={styles.ticketTitle}>{item.issue_subject}</Text>
                  <Text>{item.issue_description}</Text>
                  {ticketView === 'open' && (
                    <TouchableOpacity
                      style={[styles.smallButton, styles.expandButton]}
                      onPress={() => setExpandedTicket(item)}
                    >
                      <Text style={styles.buttonText}>Resolve Ticket</Text>
                    </TouchableOpacity>
                  )}
                </View>
              )}
              keyExtractor={(item) => item.ticket_id}
            />
          )}
        </View>
      )}

      {/* Users View */}
      {view === 'users' && (
        <ScrollView>
          <TextInput
            style={styles.searchInput}
            placeholder="Search users by username or email..."
            value={searchQuery}
            onChangeText={setSearchQuery}
          />
          <FlatList
            data={filteredUsers}
            renderItem={({ item }) => (
              <View style={styles.userItem}>
                <Text style={styles.userText}>Username: {item.username}</Text>
                <Text style={styles.userText}>Email: {item.email}</Text>
                <TouchableOpacity
                  style={[styles.button, styles.narrowDeleteButton]}
                  onPress={() => deleteUser(item.id)}
                >
                  <Text style={styles.buttonText}>Delete User</Text>
                </TouchableOpacity>
              </View>
            )}
            keyExtractor={(item) => item.id}
          />
        </ScrollView>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    backgroundColor: '#f5f5f5',
    width: '100%',
  },
  centerContainer: {
    alignItems: 'center',
    marginBottom: 10,
  },
  navContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
  },
  smallButton: {
    padding: 8,
    marginHorizontal: 5,
    borderRadius: 5,
    alignItems: 'center',
    width: 190,
  },
  activeButton: {
    backgroundColor: '#0056b3',
  },
  deleteUsersButton: {
    backgroundColor: '#FF5555', // Light red
  },
  openTicketButton: {
    backgroundColor: '#ffa500', // Orange
  },
  resolvedTicketButton: {
    backgroundColor: '#4caf50', // Green
  },
  expandButton: {
    backgroundColor: '#007bff',
    marginTop: 10,
  },
  cancelButton: {
    backgroundColor: '#ff6347', // Tomato red for cancel button
    marginTop: 10,
  },
  ticketNavContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginBottom: 10,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
    marginTop: 20,
  },
  ticketItem: {
    padding: 10,
    backgroundColor: '#fff',
    borderRadius: 5,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  ticketTitle: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  responseInput: {
    height: 40,
    borderColor: '#ddd',
    borderWidth: 1,
    borderRadius: 5,
    marginVertical: 10,
    padding: 8,
  },
  button: {
    backgroundColor: '#007bff',
    padding: 10,
    borderRadius: 5,
    alignItems: 'center',
  },
  buttonText: {
    color: '#3B3B3B',
    fontWeight: 'bold',
  },
  userItem: {
    padding: 10,
    backgroundColor: '#fff',
    borderRadius: 5,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  userText: {
    fontSize: 14,
  },
  narrowDeleteButton: {
    backgroundColor: '#FF5555', // Light red for delete button
    marginTop: 10,
    width: 100, // Make the delete button narrower
  },
  errorText: {
    color: 'red',
    marginBottom: 10,
  },
  searchInput: {
    height: 40,
    borderColor: '#ddd',
    borderWidth: 1,
    borderRadius: 5,
    marginVertical: 10,
    padding: 8,
  },
});

export default AdminPage;
