import React, { useEffect, useState } from "react";
import { useStripe } from "@stripe/react-stripe-js";

const STATUS_CONTENT_MAP = {
  succeeded: {
    text: "Payment succeeded",
    iconColor: "#30B130",
    icon: "✔️",
  },
  processing: {
    text: "Your payment is processing.",
    iconColor: "#6D6E78",
    icon: "⏳",
  },
  requires_payment_method: {
    text: "Your payment was not successful, please try again.",
    iconColor: "#DF1B41",
    icon: "❌",
  },
  default: {
    text: "Something went wrong, please try again.",
    iconColor: "#DF1B41",
    icon: "⚠️",
  },
};

export default function CompletePage() {
  const stripe = useStripe();
  const [status, setStatus] = useState("default");
  const [intentId, setIntentId] = useState(null);

  

  useEffect(() => {
    if (!stripe) return;

    // Retrieve client secret from URL parameters
    const clientSecret = new URLSearchParams(window.location.search).get(
      "payment_intent_client_secret"
    );

    if (!clientSecret) {
      setStatus("default");
      return;
    }

    stripe.retrievePaymentIntent(clientSecret).then(({ paymentIntent }) => {
      if (paymentIntent) {
        setStatus(paymentIntent.status);
        setIntentId(paymentIntent.id);
      } else {
        setStatus("default");
      }
    });
  }, [stripe]);

  return (
    <div style={styles.outerContainer}>
      <div style={styles.container}>
        <div
          style={{
            ...styles.iconContainer,
            backgroundColor: STATUS_CONTENT_MAP[status]?.iconColor,
          }}
        >
          {STATUS_CONTENT_MAP[status]?.icon}
        </div>
        <h2 style={styles.statusText}>{STATUS_CONTENT_MAP[status]?.text}</h2>
        {intentId && (
          <div style={styles.detailsTable}>
            <div style={styles.row}>
              <span style={styles.label}>ID:</span>
              <span style={styles.content}>{intentId}</span>
            </div>
            <div style={styles.row}>
              <span style={styles.label}>Status:</span>
              <span style={styles.content}>{status}</span>
            </div>
          </div>
        )}
        <a href="/checkout" style={styles.retryButton}>
          Try Another Payment
        </a>
      </div>
    </div>
  );
}

const styles = {
  outerContainer: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    height: "100vh", // Full vertical height
    width: "100vw", // Full horizontal width
    backgroundColor: "#f2f2f2", // Light background color
    fontFamily: "'Poppins', sans-serif", // Rounded font
  },
  container: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    padding: "30px",
    backgroundColor: "#fff", // White background for rectangle
    borderRadius: "15px", // Rounded edges
    boxShadow: "0px 4px 12px rgba(0, 0, 0, 0.1)", // Subtle shadow
    width: "400px", // Fixed width
    textAlign: "center", // Center text alignment
  },
  iconContainer: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    width: "60px",
    height: "60px",
    borderRadius: "50%",
    marginBottom: "20px",
  },
  statusText: {
    fontSize: "20px",
    fontWeight: "bold",
    marginBottom: "20px",
    color: "#333",
    fontFamily: "'Poppins', sans-serif", // Rounded font
  },
  detailsTable: {
    width: "100%",
    marginBottom: "20px",
    fontFamily: "'Poppins', sans-serif", // Rounded font
  },
  row: {
    display: "flex",
    justifyContent: "space-between",
    marginBottom: "10px",
  },
  label: {
    fontWeight: "bold",
  },
  content: {
    color: "#555",
  },
  retryButton: {
    display: "inline-block",
    padding: "10px 20px",
    fontSize: "16px",
    fontWeight: "bold",
    color: "#fff",
    backgroundColor: "#27ae60", // Green from the menu
    borderRadius: "10px", // Rounded edges
    textDecoration: "none",
    cursor: "pointer",
    transition: "background-color 0.3s ease",
    boxShadow: "0px 4px 6px rgba(0, 0, 0, 0.1)", // Subtle shadow for the button
  },
  retryButtonHover: {
    backgroundColor: "#1e8e50", // Darker green on hover
  },
};
