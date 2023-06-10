  
import java.util.ArrayList;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
	// Assume all functions except getProposals() will use "this" to refer the variables itself.
	// That way, it simplifies the code syntax and avoids repetitive formatting.
	private double pgraph;
	private double pmalicious;
	private double ptxDistribution;
	private int nRounds;
	private boolean[] follow;
	private Set<Transaction> pendingTransaction;
	private Set<Transaction> proposals;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.pgraph = p_graph;
        this.pmalicious = p_malicious;
        this.ptxDistribution = p_txDistribution;
        this.nRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.follow = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransaction = pendingTransactions;
    }

    public Set<Transaction> getProposals() {
         //Set<Transaction> proposals = new HashSet<>(pendingTransactions);
        //return proposals;
	return this.pendingTransaction;
    }

    public void receiveCandidates(ArrayList<Integer[]> candidates) {
        // iteratate throught the list of candidates 

        for(int i = 0; i < candidates.size();++i){
            if(follow[candidates.get(i)[1]]){ // this is true check if each candidate is a followe 
                pendingTransaction.add(new Transaction(candidates.get(i)[0])); // if it is a followe then all you do i just make it false 
            }

        }
        
    }
}
