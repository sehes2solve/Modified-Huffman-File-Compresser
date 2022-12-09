import java.util.*;

class Node
{
    float freq;
    char ch;
    Node left,right;
    public Node()
    {
        freq = 0;
        ch = '*';
        left = null;
        right = null;
    }
    public Node(float f,Node l,Node r)
    {
        freq = f;
        ch = '-';
        left = l;
        right = r;
    }
    public Node(float f,char c)
    {
        freq = f;
        ch = c;
        left = null;
        right = null;
    }
};

public class Main
{
    public static int dict_idx = 0;
    public static void binary_code_generator(int n,String s,TreeMap<Character,String> fixed_dict)
    {
        /** in case number of fixed is not divisible by 2 **/
        if(dict_idx >= fixed_dict.size())
            return;
        if(s.length() == Math.ceil(Math.log(n)/Math.log(2)))
        {
            fixed_dict.put((Character) fixed_dict.keySet().toArray()[dict_idx],s);
            dict_idx++;
            return;
        }
        binary_code_generator(n,s + '0',fixed_dict);
        binary_code_generator(n,s + '1',fixed_dict);
    }
    public static void set_fixed_length_codes(TreeMap<Character,String> fixed_dict)
    {
        dict_idx = 0;
        binary_code_generator(fixed_dict.size(),"",fixed_dict);
    }
    public static HashMap<Character,Float> build_freq_table(String msg,TreeMap<Character,String> fixed_dict)
    {
        Comparator<HashMap.Entry<Character,Float>> freq_compr = new Comparator<HashMap.Entry<Character, Float>>() {
            @Override
            public int compare(Map.Entry<Character, Float> o1, Map.Entry<Character, Float> o2)
            {
                if(o1.getValue() < o2.getValue()) return -1;
                else if(o1.getValue() > o2.getValue()) return 1;
                else return 0;
            }
        };
        Comparator<HashMap.Entry<Character,Float>> char_compr = new Comparator<HashMap.Entry<Character, Float>>() {
            @Override
            public int compare(Map.Entry<Character, Float> o1, Map.Entry<Character, Float> o2)
            {
                if(o1.getKey() < o2.getKey()) return -1;
                else if(o1.getKey() > o2.getKey()) return 1;
                else return 0;
            }
        };
        HashMap<Character,Float> freq_table = new HashMap<>();
        for(char ch : msg.toCharArray())
        {
            if(freq_table.containsKey(ch)) freq_table.put(ch,(1f/msg.length()) + freq_table.get(ch));
            else                           freq_table.put(ch,1f/msg.length());
            fixed_dict.put(ch,"");
        }
        set_fixed_length_codes(fixed_dict);
        List<HashMap.Entry<Character, Float>> temp = new ArrayList<HashMap.Entry<Character, Float>>(freq_table.entrySet());
        Collections.sort(temp,freq_compr);
        float others_freq = 0;
        HashMap.Entry<Character, Float> other;
        /** max num of codes 4 & max other freq 0.2**/
        while (temp.size() > 4 && temp.get(0).getValue() < 0.2)
        {
            other = temp.remove(0);
            others_freq += other.getValue();
        }
        Collections.sort(temp,char_compr);
        freq_table.clear();
        for(HashMap.Entry<Character, Float> itr : temp)
            freq_table.put(itr.getKey(),itr.getValue());
        freq_table.put((char)127,others_freq);
        return freq_table;
    }

    public static Node build_tree(HashMap<Character,Float> freq_table,HashMap<Character,String> dict)
    {
        ArrayList<Node> not_child_nodes  = new ArrayList<>();
        Comparator<Node> node_compr = new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                {
                  if     (o1.freq < o2.freq)  return 1;
                  else if(o1.freq > o2.freq)  return-1;
                  else if(o1.ch == (char)127) return 1;
                  else if(o2.ch == (char)127) return-1;
                  else return 0;
                }
            }
        };
        for(Character en: freq_table.keySet())
        {
            not_child_nodes.add(new Node(freq_table.get(en),en));
            dict.put(en,"");
        }
        Collections.sort(not_child_nodes,node_compr);
        while (not_child_nodes.size() > 2)
        {
            Node r_smallest = not_child_nodes.remove(not_child_nodes.size() - 1);
            Node l_before_smallest = not_child_nodes.remove(not_child_nodes.size() - 1);
            Node n = new Node(r_smallest.freq + l_before_smallest.freq, l_before_smallest, r_smallest);
            not_child_nodes.add(n);
            Collections.sort(not_child_nodes,node_compr);
        }
        Node root = new Node();
        if(not_child_nodes.size() > 0) root.left = not_child_nodes.get(0);
        if(not_child_nodes.size() > 1) root.right = not_child_nodes.get(1);
        return root;
    }
    public static void preorder_DFT(Node curr_node,String curr_code,HashMap<Character,String> dict)
    {
        if(curr_node == null) return;
        if(curr_node.left == null && curr_node.right == null) { dict.put(curr_node.ch,curr_code); return; }
        preorder_DFT(curr_node.left ,curr_code + '0',dict);
        preorder_DFT(curr_node.right,curr_code + '1',dict);
    }
    public static HashMap<Character,String> build_dict(HashMap<Character,Float> freq_table)
    {
        HashMap<Character,String> dict = new HashMap<>();
        preorder_DFT(build_tree(freq_table,dict),"",dict);
        return dict;
    }

    public static String compress(String msg,HashMap<Character,String> dict,TreeMap<Character,String> fixed_length_dict)
    {
        String code = "";
        for(int i = 0;i < msg.length();i++)
            if(dict.containsKey(msg.charAt(i)))
                code += dict.get(msg.charAt(i));
            else
                code += dict.get((char)127) + fixed_length_dict.get(msg.charAt(i));
        return code;
    }
    public static String decompress(String code,HashMap<Character,String> dict,TreeMap<Character,String> fixed_length_dict)
    {
        String msg = "",curr_sub_code = "";
        for(int i = 0;i < code.length();i++)
        {
            curr_sub_code += code.charAt(i);
            for(HashMap.Entry<Character,String> pair : dict.entrySet())
                if(pair.getValue().equals(curr_sub_code))
                {
                    if(pair.getKey() == (char)127)
                    {
                        String temp = code.substring(i + 1,i + 5);
                        for(char ch : fixed_length_dict.keySet())
                            if(fixed_length_dict.get(ch).equals(temp))
                            {
                                msg += ch;
                                break;
                            }
                        i += 4;
                    }
                    else
                        msg += pair.getKey();
                    curr_sub_code = "";
                }
        }
        return msg;
    }
    public static void main(String[] args)
    {
        String msg = "abcazdafcqdadcuabapd";
        TreeMap<Character,String> fixed_length_dict = new TreeMap<>();
        HashMap<Character,Float> freq_table = build_freq_table(msg,fixed_length_dict);
        HashMap<Character,String> dict = build_dict(freq_table);
        String code = compress(msg,dict,fixed_length_dict);
        System.out.println(code);
        System.out.println(decompress(code,dict,fixed_length_dict));
    }
}
