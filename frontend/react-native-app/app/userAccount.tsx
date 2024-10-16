// src/pages/UserAccount.tsx

import React, { useEffect, useState } from 'react';
import { View, Text, Image, Button, StyleSheet, Alert } from 'react-native';
import { useNavigate } from 'react-router-dom';
import { 
  useUsername, 
  useEmail, 
  useProfileIconUrl, 
  useSessionToken 
} from '../components/userContext';

const UserAccount: React.FC = () => {
  const [sessionToken, setSessionToken] = useSessionToken();
  const [username, setUsername] = useUsername();
  const [email, setEmail] = useEmail();
  const [profileIconUrl, setProfileIconUrl] = useProfileIconUrl();
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    // If user data is already present, skip fetching
    if (username && email && profileIconUrl) {
      setLoading(false);
      return;
    }

    if (!sessionToken) {
      // If not logged in, redirect to login
      navigate('/login');
      return;
    }

    // Fetch user data: username, email, and profile icon
    const fetchUserData = async () => {
      try {
        // Fetch profile icon
        const iconResponse = await fetch('https://tradeagently.dev/get-profile-icon', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ session_token: sessionToken }),
        });
        const iconData = await iconResponse.json();
        console.log('Icon Data:', iconData);

        // Fetch username
        const usernameResponse = await fetch('https://tradeagently.dev/get-username', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ session_token: sessionToken }),
        });
        const usernameData = await usernameResponse.json();
        console.log('Username Data:', usernameData);

        // Fetch email
        const emailResponse = await fetch('https://tradeagently.dev/get-email', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ session_token: sessionToken }),
        });
        const emailData = await emailResponse.json();
        console.log('Email Data:', emailData);

        // Update user context
        if (usernameData.status === 'Success' && emailData.status === 'Success' && iconData.status === 'Success') {
          setUsername(usernameData.username);
          setEmail(emailData.email);
          setProfileIconUrl(iconData.url);
        } else {
          Alert.alert('Error', 'Failed to fetch user data');
        }
      } catch (error) {
        Alert.alert('Error', 'An error occurred while fetching user details.');
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [sessionToken, username, email, profileIconUrl, navigate, setUsername, setEmail, setProfileIconUrl]);

  const handleLogout = () => {
    setSessionToken(null);  // Clear session token on logout
    navigate('/login');     // Redirect to login
  };

  if (loading) {
    return (
      <View style={styles.container}>
        <Text>Loading...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.detailsBox}> {/* Added the box for user details */}
        <View>
          {profileIconUrl ? (
            <Image
              source={{ uri: profileIconUrl }} // Use the profileIconUrl from context
              style={styles.profileIcon}
            />
          ) : (
            <Text>No Profile Icon Available</Text>
          )}
        </View>
        <Text style={styles.text}>Username: {username}</Text>
        <Text style={styles.text}>Email: {email}</Text>
        <Button title="Logout" onPress={handleLogout} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'flex-start',
    alignItems: 'center',
    paddingTop: 50,
    backgroundColor: '#f0f0f0',
    width: '100%',
  },
  profileIcon: {
    width: 100,
    height: 100,
    borderRadius: 50,
    marginBottom: 20,
  },
  text: {
    fontSize: 18,
    marginBottom: 10,
    color: '#333',
  },
  detailsBox: {
    backgroundColor: '#e0e0e0', // Light gray background
    borderRadius: 10,            // Rounded corners
    padding: 20,                 // Padding inside the box
    alignItems: 'center',        // Center items inside the box
    width: '40%',                // Make the box 80% of the container width
    marginBottom: 20,            // Space below the box
  },
});

export default UserAccount;
